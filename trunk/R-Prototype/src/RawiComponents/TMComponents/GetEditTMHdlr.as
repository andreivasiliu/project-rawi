package RawiComponents.TMComponents
{
	import flash.events.Event;
	import flash.net.URLLoader;
	
	import mx.core.Application;
	
	/**
	 * Using this class to save the tmName - you cannot bind request to response
	 * so you can't know the TM name only from response
	 */
	public class GetEditTMHdlr extends Object
	{
		public function GetEditTMHdlr(tmName:String = "untitled")
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
				var etm:EditTM = new EditTM();
				(Application.application as RAWI).tabNav.addChild(etm);
				(Application.application as RAWI).tabNav.selectedChild = etm;
				etm.initFromXml(xmlData);
				etm.propsGen.initialize();
				etm.propsGen.schemaName.text = tmName;
			}
		}
	}
}
