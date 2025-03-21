/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package io.github.kyay10.kotlinassign

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirFunctionCallOrigin
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.buildUnaryArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.calleeReference
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
      else -> true
    }
  }

  private fun buildFunctionCall(variableAssignment: FirVariableAssignment) = buildFunctionCall {
    source = variableAssignment.source?.fakeElement(KtFakeSourceElementKind.AssignmentPluginAltered)
    explicitReceiver = variableAssignment.lValue
    argumentList = buildUnaryArgumentList(variableAssignment.rValue)
    calleeReference = buildSimpleNamedReference {
      source = variableAssignment.source
      name = ASSIGN_METHOD
    }
    origin = FirFunctionCallOrigin.Regular
  }
}
