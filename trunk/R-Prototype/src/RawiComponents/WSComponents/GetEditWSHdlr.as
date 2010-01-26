package RawiComponents.WSComponents
{
	import flash.events.Event;
	import flash.net.URLLoader;
	
	import mx.core.Application;
	
	public class GetEditWSHdlr extends Object
	{
		public function GetEditWSHdlr(tmName:String = "untitled")
		{
			this.tmName = tmName;
		}
		public var tmName:String = "";
		public function getEditHdlr(event:Event):void
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
				ews.initFromXml(xmlData);
				ews.propsGen.initialize();
				ews.propsGen.schemaName.text = tmName;
			}
		}
	}
}