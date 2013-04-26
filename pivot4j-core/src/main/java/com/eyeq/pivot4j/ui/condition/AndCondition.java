/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.eyeq.pivot4j.ui.RenderContext;

public class AndCondition extends AbstractCondition {

	public static final String NAME = "AND";

	private List<Condition> subConditions;

	/**
	 * @param conditionFactory
	 */
	public AndCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param subCondition
	 */
	public AndCondition(ConditionFactory conditionFactory,
			Condition... subConditions) {
		super(conditionFactory);

		if (subConditions != null) {
			this.subConditions = new LinkedList<Condition>(
					Arrays.asList(subConditions));
		}
	}

	/**
	 * @param conditionFactory
	 */
	public AndCondition(ConditionFactory conditionFactory,
			List<Condition> subConditions) {
		super(conditionFactory);

		this.subConditions = subConditions;
	}

	/**
	 * @see com.eyeq.kona.equation.AbstractCondition#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @return the subConditions
	 */
	public List<Condition> getSubConditions() {
		return subConditions;
	}

	/**
	 * @param subConditions
	 *            the subConditions to set
	 */
	public void setSubConditions(List<Condition> subConditions) {
		this.subConditions = subConditions;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.Condition#matches(com.eyeq.pivot4j.ui.RenderContext)
	 */
	public boolean matches(RenderContext context) {
		if (subConditions == null || subConditions.isEmpty()) {
			throw new IllegalArgumentException("Missing sub conditions.");
		}

		for (Condition condition : subConditions) {
			if (!condition.matches(context)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		if (subConditions == null) {
			return null;
		}

		Serializable[] states = new Serializable[subConditions.size()];

		int index = 0;
		for (Condition condition : subConditions) {
			states[index++] = new Serializable[] { condition.getName(),
					condition.saveState() };
		}

		return states;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		this.subConditions = new LinkedList<Condition>();

		if (states != null) {
			for (Serializable subState : states) {
				Serializable[] subStates = (Serializable[]) subState;

				Condition condition = getConditionFactory().createCondition(
						(String) subStates[0]);
				condition.restoreState(subStates[1]);

				this.subConditions.add(condition);
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		configuration.addProperty("[@name]", getName());

		if (subConditions == null) {
			return;
		}

		int index = 0;
		for (Condition condition : subConditions) {
			HierarchicalConfiguration subConfig = configuration
					.configurationAt(String.format("condition(%s)", index++));
			condition.saveSettings(subConfig);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		List<HierarchicalConfiguration> subConfigs = configuration
				.configurationsAt("condition");

		this.subConditions = new LinkedList<Condition>();

		for (HierarchicalConfiguration subConfig : subConfigs) {
			String name = subConfig.getString("[@name]");

			if (name != null) {
				Condition condition = getConditionFactory().createCondition(
						name);
				condition.restoreSettings(subConfig);

				this.subConditions.add(condition);
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");

		if (subConditions != null) {
			boolean first = true;

			for (Condition condition : subConditions) {
				if (first) {
					first = false;
				} else {
					builder.append(" && ");
				}

				builder.append(condition.toString());
			}
		}

		builder.append(")");

		return builder.toString();
	}
}