/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package io.github.kyay10.kotlinassign

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.extensions.FirAssignExpressionAltererExtension
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedVariableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirBackingFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.types.expressions.OperatorConventions.ASSIGN_METHOD
import org.jetbrains.kotlin.utils.addToStdlib.runIf

class AssignAltererExtension(
  session: FirSession
) : FirAssignExpressionAltererExtension(session) {

  override fun transformVariableAssignment(variableAssignment: FirVariableAssignment): FirStatement? {
    return runIf(variableAssignment.supportsTransformVariableAssignment()) {
      buildFunctionCall(variableAssignment)
    }
  }

  private fun FirVariableAssignment.supportsTransformVariableAssignment(): Boolean {
    return when (val lSymbol = calleeReference?.toResolvedVariableSymbol()) {
      is FirPropertySymbol -> lSymbol.isVal
      is FirBackingFieldSymbol -> lSymbol.isVal
      is FirFieldSymbol -> lSymbol.isVal
      else -> false
    }
  }

  private fun buildFunctionCall(variableAssignment: FirVariableAssignment): FirFunctionCall {
    val leftArgument = variableAssignment.calleeReference!!
    val leftSymbol = leftArgument.toResolvedVariableSymbol()!!
    val leftResolvedType = leftSymbol.resolvedReturnTypeRef
    val rightArgument = variableAssignment.rValue
    return buildFunctionCall {
      source = variableAssignment.source?.fakeElement(KtFakeSourceElementKind.DesugaredCompoundAssignment)
      explicitReceiver = buildPropertyAccessExpression {
        source = leftArgument.source
        coneTypeOrNull = leftResolvedType.type
        calleeReference = leftArgument
        (variableAssignment.lValue as? FirQualifiedAccessExpression)?.typeArguments?.let(typeArguments::addAll)
        annotations += variableAssignment.annotations
        explicitReceiver = variableAssignment.explicitReceiver
        dispatchReceiver = variableAssignment.dispatchReceiver
        extensionReceiver = variableAssignment.extensionReceiver
        contextReceiverArguments += variableAssignment.contextReceiverArguments
      }
      argumentList = buildUnaryArgumentList(rightArgument)
      calleeReference = buildSimpleNamedReference {
        source = variableAssignment.source
        name = ASSIGN_METHOD
      }
      origin = FirFunctionCallOrigin.Regular
    }
  }
}
