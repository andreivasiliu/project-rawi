package RawiComponents.TMComponents
{
	import flash.events.*;
	import flash.net.*;
	
	import mx.controls.Alert;
	import mx.core.Application;
	
	public class SaveValidateHdlr extends Object
	{
		private var m_editor:EditTM;
		public var baseUri:String;
		public function SaveValidateHdlr(editor:EditTM)
		{
			m_editor = editor;
		}
		public function Validate():void
		{
			trace(m_editor.getSchemaXmlRepresentation());
			var loader:URLLoader = new URLLoader();
			var request:URLRequest = new URLRequest(baseUri + "/ValidateXMLServlet");
			var variables:URLVariables = new URLVariables();
			variables.time = new Date().getTime()
			variables.name = m_editor.propsGen.schemaName.text + ".xml";
			variables.xml = m_editor.getSchemaXmlRepresentation().toString();
			request.data = variables;
			request.method = URLRequestMethod.POST;
			loader.addEventListener(Event.COMPLETE, validateResponse);
			
			configureListeners(loader);
			loader.load(request);
		}
		public function Save():void
		{
			var loader:URLLoader = new URLLoader();
			var request:URLRequest = new URLRequest(baseUri + "/ValidateXMLServlet");
			var variables:URLVariables = new URLVariables();
			variables.time = new Date().getTime()
			variables.name = m_editor.propsGen.schemaName.text + ".xml";
			variables.xml = m_editor.getSchemaXmlRepresentation().toString();
			variables.name = m_editor.propsGen.schemaName.text + ".xml";
			variables.savexml = "SaveXML";
			request.data = variables;
			request.method = URLRequestMethod.POST;
			loader.addEventListener(Event.COMPLETE, saveResponse);
			
			configureListeners(loader);
			loader.load(request);
		}
		public function Close():void
		{
			// TODO: any checkes here? see if something needs saving
			(Application.application as RAWI).tabNav.removeChild(m_editor);
		}
		private function configureListeners(dispatcher:IEventDispatcher):void
		{
			// manage the other events from URLRequest
            //dispatcher.addEventListener(Event.COMPLETE, getFromToAirports);
            dispatcher.addEventListener(Event.OPEN, trace);
            dispatcher.addEventListener(ProgressEvent.PROGRESS, trace);
            dispatcher.addEventListener(SecurityErrorEvent.SECURITY_ERROR, trace);
            dispatcher.addEventListener(HTTPStatusEvent.HTTP_STATUS, trace);
            dispatcher.addEventListener(IOErrorEvent.IO_ERROR, trace);
        }
		private function validateResponse(event:Event):void
		{
			var loader:URLLoader = URLLoader(event.target);
			trace(loader.data);
			// adauga aici object.data
			if (loader.data.toString().length > 0)
			{
				var xmlData:XML = new XML(loader.data.toString());
				if (xmlData.elements("xml-success"))
					Alert.show(xmlData.elements("xml-message"), "Validating " + xmlData.elements("xml-name"));
				else
					Alert.show(xmlData.elements("xml-message"), "Error validating " + xmlData.elements("xml-name"));
			}
        }
		private function saveResponse(event:Event):void
		{
			var loader:URLLoader = URLLoader(event.target);
			trace(loader.data);
			// adauga aici object.data
			if (loader.data.toString().length > 0)
			{
				var xmlData:XML = new XML(loader.data.toString());
				if (xmlData.elements("xml-success"))
					Alert.show(xmlData.elements("xml-message"), "Saving " + xmlData.elements("xml-name"));
				else
					Alert.show(xmlData.elements("xml-message"), "Error saving" + xmlData.elements("xml-name"));
			}
        }
	}
}