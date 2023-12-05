package org.asmeta.simulator_at_runtime.runtime_composer;

/**
 * @author Michele Zenoni
 */

import java.util.Comparator;

public class SortTreeById implements Comparator<CompositionTreeNode>{
	@Override
	public int compare(CompositionTreeNode t1, CompositionTreeNode t2) {
		return t1.getID() - t2.getID();
	}
}
