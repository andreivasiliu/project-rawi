package RawiComponents
{
	import flash.events.*;
	import flash.utils.Timer;
	
	import mx.core.Application;
	
	public class GlobalSettings extends Object
	{
		public static function get baseUri():String
		{
			// return "" if you are in browser, and the full adress if you are in debugg mode
			// A.a.url returns http:// if running from browser
			if (Application.application.url.charAt(0) == 'h')
				return "/I-Prototype";
			else
				return "http://localhost:8080/I-Prototype";
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
			(Application.application as RAWI).statusLabel.text = msg;
			clearTimer.start();
			clearTimer.addEventListener(TimerEvent.TIMER_COMPLETE, endMsg);
		}
		private static function endMsg(event:TimerEvent):void
		{
			clearTimer.stop();
			(Application.application as RAWI).statusLabel.text = "";
		}
	}
}