package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.parsing.MatchExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchAnalyser {

    public static List<BindingError> analyse(BoundMatchExpression matchExpression, MatchExpression expression, BoundScope currentScope) {
        List<BindingError> errors = new ArrayList<>();

        BoundExpression operand = matchExpression.getOperand();

        if (operand.getType() instanceof EnumTypeSymbol) {
            errors.addAll(analyseEnumMatch((EnumTypeSymbol)currentScope.tryLookupType(operand.getType().getName()).get(), matchExpression, expression));
        } else {
            if (!hasElseCase(matchExpression.getMatchCaseExpressions())) {
                errors.add(BindingError.raiseNonExhaustiveMatchExpression(operand, expression.getMatchKeyword().getSpan()));
            }
        }

        return errors;
    }

    private static List<BindingError> analyseEnumMatch(EnumTypeSymbol type, BoundMatchExpression matchExpression, MatchExpression expression) {
        List<BindingError> errors = new ArrayList<>();

        List<BoundMatchCaseExpression> matchCaseExpressions = matchExpression.getMatchCaseExpressions();

        Map<String, VariableSymbol> fields = type.getFields();
        
        List<VariableSymbol> members = new ArrayList<>(fields.values());
        for (BoundMatchCaseExpression matchCaseExpression : matchCaseExpressions) {
            BoundExpression caseExpression = matchCaseExpression.getCaseExpression();
            if (caseExpression == null) {
                continue;
            }
            switch (caseExpression.getBoundExpressionType()) {
                case MEMBER_ACCESSOR:
                    BoundMemberAccessorExpression memberAccessorExpression = (BoundMemberAccessorExpression) caseExpression;
                    BoundVariableExpression member = (BoundVariableExpression) memberAccessorExpression.getMember();
                    members.remove(member.getVariable());
                    break;
                default:
                    break;
            }
        }
        if (!members.isEmpty() && !hasElseCase(matchCaseExpressions)) {
            errors.add(BindingError.raiseNonExhaustiveEnumMatchExpression(type, members, expression.getMatchKeyword().getSpan()));
        }

        return errors;
    }

    private static boolean hasElseCase(List<BoundMatchCaseExpression> matchCaseExpressions) {
        return matchCaseExpressions.get(matchCaseExpressions.size() - 1).getCaseExpression() == null;
    }

}
