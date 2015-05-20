package com.developer.bsince.core.assist;

public enum EngineType {

	Engine_Bsince(0),
	Engine_Executor(1);
	
	private int value;
	private EngineType(int value){
		this.value = value;
	}
	public int getEnginValue(){
		return value;
	}
}
