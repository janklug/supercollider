//jrh 2002//abstract class that servers as a template for any environment hacksEnvironmentRedirect {
	var <>envir;	var <>saveEnvir, <>saveTopEnvir;	
	*new { 
		^super.newCopyArgs(Environment.new)
	}		*push { 		^super.new.push;	}		*pop { 
		var curr;
		curr = currentEnvironment;
		if(curr.inside, {			currentEnvironment = curr.saveEnvir;
			topEnvironment = curr.saveTopEnvir;
		}, {
			"not a current environment".inform;
		})		}	
	push { 
		if(this.inside, { "already pushed".inform; ^this });
		this.saveEnvir = currentEnvironment;
		this.saveTopEnvir = topEnvironment;
		currentEnvironment = this;
		topEnvironment = this; //to avoid error loss
	}
	
	pop {
		this.class.pop
	}
			//override in subclasses
		at { arg key;		^envir.at(key)	}		put { arg key, obj;		envir.put(key, obj)	}
	
	
		inside {
		^currentEnvironment.isKindOf(this.class);
	}			
	// behave like my environment
	
		use { arg function;
		// temporarily replaces the currentEnvironment with this, 
		// executes function, returns the result of the function
		var result, saveEnvir;
		
		saveEnvir = currentEnvironment;
		currentEnvironment = this;
		result = function.value(this);
		currentEnvironment = saveEnvir;
		^result
	}
	
	do { arg function;
		envir.do(function)
	}
	keysValuesDo { arg function;
		envir.keysValuesDo(function);
	}
	
	keysValuesArrayDo { arg argArray, function;
		envir.keysValuesArrayDo(argArray, function);
	}
	findKeyForValue { arg val;
		^envir.findKeyForValue(val)
	}
	
	choose {
        ^envir.choose 
    }
}