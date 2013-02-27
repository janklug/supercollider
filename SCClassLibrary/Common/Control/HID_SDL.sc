HID_SDL {

	classvar <running = false;

	classvar <deviceList;
	classvar <deviceNames;

	classvar <>globalAction; // this is where HIDFunc should hook into

	*initClass{
		deviceList = IdentityDictionary.new;
	}

	*buildDeviceList{
		var devlist;
		if ( running.not ){ this.start }; // start eventloop if not yet running
		devlist = this.prbuildDeviceList;
		devlist.postln;
		if ( devlist.isKindOf( Array ) ){
			deviceNames = devlist;
			devlist.do{ |it,i|
				deviceList.put( i, HID_SDL_Device.new( i, it ) );
				// get basic info on device, and set it to the device
			}{ // no devices found
				"no HID devices found".postln;
			}
		}
	}

	*start{
		this.prStart;
		running = true;
		ShutDown.add( {this.stop} );
	}

	*stop{
		this.prStop;
		running = false;
	}

  /// private

/// primitives called:
	*prStart{
		_HID_SDL_Start;
		^this.primitiveFailed
	}

	*prStop{
		_HID_SDL_Stop;
		^this.primitiveFailed
	}

	*prOpenJoystick{ |joyid|
		_HID_SDL_OpenJoystick
		^this.primitiveFailed
	}

	*prCloseJoystick{ |joyid|
		_HID_SDL_CloseJoystick
		^this.primitiveFailed
	}

	*prGetJoystickInfo{ |joyid|
		_HID_SDL_GetInfo
		^this.primitiveFailed
	}

	*prGetJoystickDeviceMap{ |joyid|
		_HID_SDL_GetDeviceMap
		^this.primitiveFailed
	}

	*prbuildDeviceList{
		_HID_SDL_BuildDeviceList
		^this.primitiveFailed
	}

// coming from the primitives:
	*prJoystickClosed{ |devid|
		globalAction.value( \closed, devid );
		deviceList.at( devid ).closeAction.value;
		[ devid, "closed" ].postln; // debugging
	}

	*prJoystickAxis { | devid, axis, value |
		globalAction.value( \axis, devid, axis, value );
		deviceList.at( devid ).valueAction( \axis, axis, value );
		[ devid, "axis", axis, value ].postln; // debugging
	}

	*prJoystickButton { | devid, button, value |
		globalAction.value( \button, devid, button, value );
		deviceList.at( devid ).valueAction( \button, button, value );
		[ devid, "button", button, value ].postln; // debugging
	}

	*prJoystickHat { | devid, hat, value |
		globalAction.value( \hat, devid, hat, value );
		deviceList.at( devid ).valueAction( \hat, hat, value );
		[ devid, "hat", hat, value ].postln; // debugging
	}

	*prJoystickBall { | devid, ball, xvalue, yvalue |
		globalAction.value( \ball, devid, ball, xvalue, yvalue );
		deviceList.at( devid ).valueAction( \ball, ball, xvalue, yvalue );
		[ devid, "ball", ball, xvalue, yvalue ].postln; // debugging
	}
}

HID_SDL_Device {
	var <id, <name;
	var <numAxes, <numButtons, <numHats, <numBalls;
	var <guid;

	var <deviceMap; // an identitydictionary with elements
	var <elementDictionary;

	var <>action;
	var <>closeAction;
	var <>debug = false;

	*new{ |id,name|
		^super.new.init( id, name );
	}

	init{ |i,n|
		id = i;
		name = n;
		deviceMap = IdentityDictionary.new;
		elementDictionary = MultiLevelIdentityDictionary.new;
	}

	valueAction{ arg ...args;
		if ( debug ){
			([ id, name ] ++ args).postln;
		};
		action.value( *args );
//		elementDictionary.at( args[0], args[1] ).valueAction( args.copyToEnd( 2 ) ); // type, id
	}

	getDeviceMap{
		^HID_SDL.prGetJoystickDeviceMap( id );
	}

	getInfo{
		var result = HID_SDL.prGetJoystickInfo( id );
		if ( result.isKindOf( Array ) ){
			#name, numAxes, numButtons, numHats, numBalls, guid = result;
			this.prettyPrint;
		}{
			"could not get info on joystick; maybe it is not open?".postln;
		}
	}

	prettyPrint{
		"Device % has % the following properties:\n".(id, name).postln;
		[ ["number of axes",numAxes], ["number of buttons", numButtons ], [ "number of hats", numHats ], [ "number of balls", numBalls ], [ "unique identifier", guid ] ].do{ |it|
			"\t % : % ".( it[0], it[1] ).postln;
		};
	}

	open{
		^HID_SDL.prOpenJoystick( id );
	}

	close{
		^HID_SDL.prCloseJoystick( id );
	}
}

HID_SDL_Element{
	var <key, <type, <id;
	var <value, <>action;

	*new{ |key, type, id|
		^super.new.init( key, type, id ) ;
	}

	init{ |k,t,i|
		key = k;
		type = t;
		id = i;
	}

	valueAction{ arg ...args;
		action.value( *args );
		value = args;
	}
}