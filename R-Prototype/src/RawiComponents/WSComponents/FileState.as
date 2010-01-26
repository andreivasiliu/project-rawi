package RawiComponents.WSComponents
{
	public class FileState extends Object
	{
		static public function get EMPTY():String { return "empty"; }
		static public function get SELECTED():String { return "selected"; }
		static public function get UPLOADING():String { return "uploading"; }
		static public function get READY():String { return "ready"; }
		static public function get ERROR():String { return "error"; }
		public var state:String = EMPTY;
	}
}