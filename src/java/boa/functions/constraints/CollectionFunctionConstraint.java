package boa.functions.constraints;

import boa.types.BoaType;

public class CollectionFunctionConstraint implements BoaFunctionConstraint{

	private int collectionPos;
	private int itemPos;
	
	@Override
	public boolean isValid(Object[] ids) {
		// TODO Auto-generated method stub
		
		String collDef = ((BoaType) ids[collectionPos]).toString();
		String itemDef = ((BoaType) ids[itemPos]).toString();
		
		String[] parts = collDef.split(" ");
		if(parts[parts.length-1].equals(itemDef))
			return true;
		else
			return false;
	}
	
	public CollectionFunctionConstraint(int collectionPos, int itemPos){
		this.collectionPos = collectionPos;
		this.itemPos = itemPos;
		
	}

}
