package RawiComponents.TMComponents
{
	import RawiComponents.GlobalSettings;
	
	import flash.events.*;
	import flash.net.*;
	
	import mx.controls.Alert;
	import mx.core.Application;
	import mx.events.CloseEvent;
	
	public class SaveValidateHdlr extends Object
	{
		private var m_editor:EditTM;
		public var savedVersion:String = "";
		public function SaveValidateHdlr(editor:EditTM)
		{
			m_editor = editor;
		}
		public function Validate():void
		{
			trace(m_editor.getSchemaXmlRepresentation());
			var loader:URLLoader = new URLLoader();
			var request:URLRequest = new URLRequest(GlobalSettings.baseUri + "/ValidateXMLServlet");
			var variables:URLVariables = new URLVariables();
			variables.time = new Date().getTime()
			variables.name = m_editor.propsGen.schemaName.text + ".xml";
			variables.xml = m_editor.getSchemaXmlRepresentation().toString();
			request.data = variables;
			request.method = URLRequestMethod.POST;
			loader.addEventListener(Event.COMPLETE, validateResponse);
			
			GlobalSettings.configureListeners(loader);
			loader.load(request);
			trace("Validate", m_editor.propsGen.schemaName.text + ".xml");
		}
		public function Save():void
		{
			var loader:URLLoader = new URLLoader();
			var request:URLRequest = new URLRequest(GlobalSettings.baseUri + "/ValidateXMLServlet");
			var variables:URLVariables = new URLVariables();
			variables.time = new Date().getTime()
			variables.name = m_editor.propsGen.schemaName.text + ".xml";
			variables.xml = m_editor.getSchemaXmlRepresentation().toString();
			variables.savexml = "SaveXML";
			request.data = variables;
			request.method = URLRequestMethod.POST;
			loader.addEventListener(Event.COMPLETE, saveResponse);
			
			GlobalSettings.configureListeners(loader);
			loader.load(request);
			trace("Save", m_editor.propsGen.schemaName.text + ".xml");
		}
		public function Close():void
		{
			// if the current version of the schema is different from the last one saved - ask for permision to close
			if (this.m_editor.getSchemaXmlRepresentation().toString() != savedVersion)
				Alert.show("You have unsaved modifications, close anyway?", "", Alert.YES | Alert.CANCEL, null, alertCloseHdlr);
			else
				(Application.application as RAWI).tabNav.removeChild(m_editor);
		}
		private function alertCloseHdlr(event:CloseEvent):void
		{
			if (event.detail == Alert.YES)
				(Application.application as RAWI).tabNav.removeChild(m_editor);
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
				{
					Alert.show(xmlData.elements("xml-message"), "Saving " + xmlData.elements("xml-name"));
					savedVersion = m_editor.getSchemaXmlRepresentation().toString();
					(Application.application as RAWI).viewTmList.refreshHdlr();
				}
				else
					Alert.show(xmlData.elements("xml-message"), "Error saving" + xmlData.elements("xml-name"));
			}
        }
	}
}