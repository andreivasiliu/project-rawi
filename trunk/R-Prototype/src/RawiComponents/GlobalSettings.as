package RawiComponents
{
	import flash.events.*;
	import flash.utils.Timer;
	
	import mx.core.Application;
	
	public class GlobalSettings extends Object
	{
		public static function get baseUri():String
		{
			// return "/I-Prototype" if you are in browser, and the full adress if you are in debugg mode
			// A.a.url returns http:// if running from browser
			if (Application.application.url.charAt(0) == 'h')
				return ""; //"/I-Prototype";
			else
				return "http://localhost:8083"; ///I-Prototype";
		}
		public static function configureListeners(dispatcher:IEventDispatcher):void
		{
			// manage the other events from URLRequest
			//dispatcher.addEventListener(Event.COMPLETE, getFromToAirports);
			dispatcher.addEventListener(Event.OPEN, trace);
			dispatcher.addEventListener(ProgressEvent.PROGRESS, trace);
			dispatcher.addEventListener(SecurityErrorEvent.SECURITY_ERROR, trace);
			dispatcher.addEventListener(HTTPStatusEvent.HTTP_STATUS, trace);
			dispatcher.addEventListener(IOErrorEvent.IO_ERROR, trace);
		}
		private static var clearTimer:Timer = new Timer(5000);

		public static function alert(msg:String):void
		{
			// displays the msg string on the RAWI bar for 5 seconds
			(Application.application as RAWI).statusLabel.text = msg;
			clearTimer.start();
			clearTimer.addEventListener(TimerEvent.TIMER_COMPLETE, endMsg);
		}
		private static function endMsg(event:TimerEvent):void
		{
			// clears the message from RAWI bar
			clearTimer.stop();
			(Application.application as RAWI).statusLabel.text = "";
		}
		
		public static var propsFileOffset:int = 0;		// used by propsPN to request only some files on WS update
		public static var propsFileAmount:int = 10;		// the same to request a certain amount of files on WS update 
	}
}