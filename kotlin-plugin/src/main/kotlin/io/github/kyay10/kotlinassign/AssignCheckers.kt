/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package io.github.kyay10.kotlinassign

import io.github.kyay10.kotlinassign.AssignError.CALL_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT
import io.github.kyay10.kotlinassign.AssignError.DECLARATION_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT
import io.github.kyay10.kotlinassign.AssignError.NO_APPLICABLE_ASSIGN_METHOD
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.DECLARATION_RETURN_TYPE
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.OPERATOR
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirSimpleFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.isError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeAmbiguityError
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeDiagnosticWithSingleCandidate
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedNameError
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isUnit
import org.jetbrains.kotlin.types.expressions.OperatorConventions.ASSIGN_METHOD

object AssignError {
  val DECLARATION_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT by
      error0<PsiElement>(DECLARATION_RETURN_TYPE)
  val CALL_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT by error0<PsiElement>(OPERATOR)
  val NO_APPLICABLE_ASSIGN_METHOD by error0<PsiElement>(OPERATOR)

  init {
    RootDiagnosticRendererFactory.registerFactory(AssignDefaultErrorMessages)
  }
}

class AssignCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {

  override val declarationCheckers: DeclarationCheckers =
      object : DeclarationCheckers() {
        override val simpleFunctionCheckers: Set<FirSimpleFunctionChecker>
          get() = setOf(AssignFunctionChecker)
      }

  override val expressionCheckers: ExpressionCheckers =
      object : ExpressionCheckers() {
        override val functionCallCheckers: Set<FirFunctionCallChecker>
          get() = setOf(AssignCallChecker)
      }
}

object AssignFunctionChecker : FirSimpleFunctionChecker(MppCheckerKind.Common) {

  override fun check(
      declaration: FirSimpleFunction,
      context: CheckerContext,
      reporter: DiagnosticReporter
  ) {
    if (declaration.origin != FirDeclarationOrigin.Source) return
    if (!declaration.isAssignMethod()) return

    if (!declaration.returnTypeRef.coneType.isUnit) {
      reporter.reportOn(
          declaration.source, DECLARATION_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT, context)
    }
  }

  private fun FirSimpleFunction.isAssignMethod(): Boolean {
    return valueParameters.size == 1 && this.name == ASSIGN_METHOD
  }
}

object AssignCallChecker : FirFunctionCallChecker(MppCheckerKind.Common) {
  override fun check(
      expression: FirFunctionCall,
      context: CheckerContext,
      reporter: DiagnosticReporter
  ) {
    if (!expression.isOverloadAssignCallCandidate()) return

    val calleeReference = expression.calleeReference
    if (calleeReference.isError()) {
      if (expression.isOverloadedAssignCallError(calleeReference.diagnostic)) {
        reporter.reportOn(expression.source, NO_APPLICABLE_ASSIGN_METHOD, context)
      }
    } else if (expression.isOverloadedAssignCall && !expression.isReturnTypeUnit) {
      reporter.reportOn(expression.source, CALL_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT, context)
    }
  }

  private fun FirFunctionCall.isOverloadAssignCallCandidate() =
      arguments.size == 1 && source?.kind == KtFakeSourceElementKind.AssignmentPluginAltered

  private fun FirFunctionCall.isOverloadedAssignCallError(diagnostic: ConeDiagnostic): Boolean {
    val functionName =
        when (diagnostic) {
          is ConeAmbiguityError -> diagnostic.name
          is ConeDiagnosticWithSingleCandidate -> diagnostic.candidate.callInfo.name
          is ConeUnresolvedNameError -> diagnostic.name
          else -> calleeReference.name
        }
    return functionName == ASSIGN_METHOD
  }

  private val FirFunctionCall.isOverloadedAssignCall
    get() = calleeReference.name == ASSIGN_METHOD

  private val FirFunctionCall.isReturnTypeUnit
    get() = toResolvedCallableSymbol()?.resolvedReturnType?.isUnit ?: false
}

object AssignDefaultErrorMessages : BaseDiagnosticRendererFactory() {
  override val MAP =
      KtDiagnosticFactoryToRendererMap("ValueContainerAssignment").apply {
        put(
            DECLARATION_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT,
            "Function 'assign' used for '=' overload should return 'Unit'")

        put(
            CALL_ERROR_ASSIGN_METHOD_SHOULD_RETURN_UNIT,
            "Function 'assign' used for '=' overload should return 'Unit'")

        put(NO_APPLICABLE_ASSIGN_METHOD, "No applicable 'assign' function found for '=' overload")
      }
}
