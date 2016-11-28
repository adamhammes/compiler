/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.compiler;

import java.util.Arrays;
import java.util.HashMap;

import boa.functions.constraints.BoaFunctionConstraint;
import boa.types.BoaFunction;
import boa.types.BoaName;
import boa.types.BoaStack;
import boa.types.BoaType;
import boa.types.BoaTypeVar;
import boa.types.BoaVarargs;


/**
 * @author anthonyu
 * @author rdyer
 */
public class FunctionTrie {
	
	private BoaFunctionConstraint[] functionConstraints;
	@SuppressWarnings("rawtypes")
	private final HashMap trie;

	@SuppressWarnings("rawtypes")
	public FunctionTrie() {
		this.trie = new HashMap();
	}

	private BoaFunction getFunction(final Object[] ids) {
		if (this.trie.containsKey(ids[0])) {
			if (ids[0].equals(""))
				return getFunction();
			else
				return ((FunctionTrie) this.trie.get(ids[0])).getFunction(Arrays.copyOfRange(ids, 1, ids.length));
		} else {
			for (final Object o : this.trie.keySet()) {
				if (o instanceof BoaVarargs && ((BoaVarargs) o).accepts((BoaType) ids[0]))
					return ((FunctionTrie) this.trie.get(o)).getFunction();

				if (o instanceof BoaType && !(ids[0] instanceof String) && ((BoaType) o).accepts((BoaType) ids[0])) {
					final BoaFunction function = ((FunctionTrie) this.trie.get(o)).getFunction(Arrays.copyOfRange(ids, 1, ids.length));

					if (function != null)
						return function;
				}
			}
		}

		return null;
	}

	private BoaFunction getFunction() {
		return (BoaFunction) this.trie.get("");
	}

	public boolean hasFunction(final String name) {
		return this.trie.containsKey(name);
	}

	public BoaFunction getFunction(final String name, final BoaType[] formalParameters) {
		final Object[] ids = new Object[formalParameters.length + 2];

		ids[0] = name;

		for (int i = 0; i < formalParameters.length; i++)
			ids[i + 1] = formalParameters[i];

		ids[ids.length - 1] = "";

		return this.getFunction(ids);
	}
	
	public BoaFunction checkFunction(final String name, final BoaType[] formalParameters) {
	
		BoaFunction function = this.getFunction(name, formalParameters);
		if(function == null)
			return null;
		
		if(this.functionConstraints == null)
			return function;
		
		for (int i = 0; i < this.functionConstraints.length; i++){
			if(!this.functionConstraints[i].isValid(formalParameters))
				return null;
		}
			
			return function;
		
	}
	@SuppressWarnings("unchecked")
	private void addFunction(final Object[] ids, final BoaFunction boaFunction) {
		if (this.trie.containsKey(ids[0])) {
			if (ids[0].equals("")) {
				throw new RuntimeException("function " + boaFunction + " already defined");
			} else {
				((FunctionTrie) this.trie.get(ids[0])).addFunction(Arrays.copyOfRange(ids, 1, ids.length), boaFunction);
			}
		} else {
			if (ids[0].equals("")) {
				this.trie.put("", boaFunction);
			} else {
				final FunctionTrie functionTrie = new FunctionTrie();
				functionTrie.addFunction(Arrays.copyOfRange(ids, 1, ids.length), boaFunction);
				this.trie.put(ids[0], functionTrie);
			}
		}
	}

	public void addFunction(final String name, final BoaFunction boaFunction) {
		final BoaType[] formalParameters = boaFunction.getFormalParameters();

		final Object[] ids = new Object[formalParameters.length + 2];

		ids[0] = name;

		for (int i = 0; i < formalParameters.length; i++)
			if (formalParameters[i] instanceof BoaName)
				ids[i + 1] = ((BoaName)formalParameters[i]).getType();
			else
				ids[i + 1] = formalParameters[i];

		ids[ids.length - 1] = "";

		this.addFunction(ids, boaFunction);
	}
	
	public void addFunction(final String name, final BoaFunction boaFunction, BoaFunctionConstraint[] constraints) {
		this.functionConstraints = constraints;
		this.addFunction(name, boaFunction);		
	}
}
