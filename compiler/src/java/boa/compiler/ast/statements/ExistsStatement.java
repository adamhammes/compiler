package boa.compiler.ast.statements;

import boa.compiler.ast.Component;
import boa.compiler.ast.Node;
import boa.compiler.ast.expressions.Expression;
import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArg;
import boa.parser.Token;

/**
 * 
 * @author rdyer
 */
public class ExistsStatement extends Statement {
	protected Component var;
	protected Expression condition;
	protected Block body;

	public Component getVar() {
		return var;
	}

	public Expression getCondition() {
		return condition;
	}

	public void setCondition(final Expression e) {
		condition = e;
	}

	public Block getBody() {
		return body;
	}

	public ExistsStatement(final Component var, final Expression condition, final Statement s) {
		this(var, condition, Node.ensureBlock(s));
	}

	public ExistsStatement(final Component var, final Expression condition, final Block body) {
		if (var != null)
			var.setParent(this);
		if (condition != null)
			condition.setParent(this);
		if (body != null)
			body.setParent(this);
		this.var = var;
		this.condition = condition;
		this.body = body;
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitor<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(final AbstractVisitorNoArg v) {
		v.visit(this);
	}

	public ExistsStatement clone() {
		final ExistsStatement s = new ExistsStatement(var.clone(), condition.clone(), body.clone());
		copyFieldsTo(s);
		return s;
	}

	public ExistsStatement setPositions(final Token first, final Node last) {
		return (ExistsStatement)setPositions(first.beginLine, first.beginColumn, last.endLine, last.endColumn);
	}
}