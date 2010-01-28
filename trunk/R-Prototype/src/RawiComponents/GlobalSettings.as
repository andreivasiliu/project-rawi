package RawiComponents
{
	import flash.events.*;
	
	public class GlobalSettings extends Object
	{
		public static var baseUri:String = "http://localhost:8080/I-Prototype";
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
	}
}