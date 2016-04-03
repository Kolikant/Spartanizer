package il.org.spartan.refactoring.wring;

import static il.org.spartan.refactoring.utils.Funcs.*;
import static il.org.spartan.refactoring.wring.Wrings.size;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEditGroup;

import il.org.spartan.refactoring.preferences.PluginPreferencesResources.WringGroup;
import il.org.spartan.refactoring.utils.Extract;
import il.org.spartan.refactoring.utils.Is;
import il.org.spartan.refactoring.utils.Subject;
import il.org.spartan.refactoring.wring.LocalInliner.LocalInlineWithValue;

/**
 * A {@link Wring} to convert <code>int a = 2;
 * if (b)
 *   a = 3;</code> into <code>int a = b ? 3 : 2;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-07
 */
public final class DeclarationInitializerIfUpdateAssignment extends Wring.VariableDeclarationFragementAndStatement {
  @Override ASTRewrite go(final ASTRewrite r, final VariableDeclarationFragment f, final SimpleName n, final Expression initializer, final Statement nextStatement,
      final TextEditGroup g) {
    if (initializer == null)
      return null;
    final IfStatement s = asIfStatement(nextStatement);
    if (s == null || !Is.vacuousElse(s))
      return null;
    s.setElseStatement(null);
    final Expression condition = s.getExpression();
    final Assignment a = Extract.assignment(then(s));
    if (a == null || !same(left(a), n) || doesUseForbiddenSiblings(f, condition, right(a)))
      return null;
    final Operator o = a.getOperator();
    if (o == Assignment.Operator.ASSIGN)
      return null;
    final ConditionalExpression newInitializer = Subject.pair(assignmentAsExpression(a), initializer).toCondition(condition);
    final LocalInlineWithValue i = new LocalInliner(n, r, g).byValue(initializer);
    if (!i.canInlineInto(newInitializer) || i.replacedSize(newInitializer) - size(nextStatement, initializer) > 0)
      return null;
    r.replace(initializer, newInitializer, g);
    i.inlineInto(then(newInitializer), newInitializer.getExpression());
    r.remove(nextStatement, g);
    return r;
  }
  @Override public String description(final VariableDeclarationFragment f) {
    return "Consolidate initialization of " + f.getName() + " with the subsequent conditional assignment to it";
  }
  @Override WringGroup wringGroup() {
	return WringGroup.IF_TO_TERNARY;
  }
}