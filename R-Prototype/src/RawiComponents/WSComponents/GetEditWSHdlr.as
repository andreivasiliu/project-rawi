package RawiComponents.WSComponents
{
	import RawiComponents.GlobalSettings;
	
	import flash.events.Event;
	import flash.net.URLLoader;
	import flash.net.URLRequest;
	
	import mx.core.Application;
	
	public class GetEditWSHdlr extends Object
	{
		public var sessionId:String = "";
		public var tmName:String = "";
		public function getCreateSessionHdlr(event:Event):void	// invoked when createSession returns
		{
			var loader:URLLoader = URLLoader(event.target);
			//trace(loader.data);
			// adauga aici object.data
			if (loader.data.toString().length > 0)
			{
				var xmlData:XML = new XML(loader.data.toString());
				//trace (xmlData);
				viewEditHdlr(xmlData.sessionId, tmName);
			}
		}
		public function viewEditHdlr(sid:String, ptmName:String):void	// invoked when EditWS is clicked in ViewWSList 
		{
			this.sessionId = sid;
			this.tmName = ptmName;
			var loader:URLLoader = new URLLoader();
			// TODO: find out how to cancel the cache - by then add the time parameter at the end 
			var request:URLRequest = new URLRequest(GlobalSettings.baseUri + "/DownloadXMLServlet?sessionId=" + sessionId + "&time=" + new Date().getTime());
			trace(request.url);
			loader.addEventListener(Event.COMPLETE, getTmXml);
			GlobalSettings.configureListeners(loader);
			loader.load(request);
			// refresh the ViewWSList when creating a new working session
			if ((Application.application as RAWI).viewWsList.initialized)
				(Application.application as RAWI).viewWsList.refreshHdlr();
		}
		private function getTmXml(event:Event):void
		{
			var loader:URLLoader = URLLoader(event.target);
			//trace(loader.data);
			// adauga aici object.data
			if (loader.data.toString().length > 0)
			{
				var xmlData:XML = new XML(loader.data.toString());
				//trace (xmlData);
				var ews:EditWS = new EditWS();
				(Application.application as RAWI).tabNav.addChild(ews);
				(Application.application as RAWI).tabNav.selectedChild = ews;
				trace("getTmXml", sessionId); 
				ews.sessionState.sessionId = this.sessionId;
				ews.initFromXml(xmlData, tmName);
			}
		}
	}
}