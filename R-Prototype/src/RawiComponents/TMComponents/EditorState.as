package RawiComponents.TMComponents
{
	public class EditorState extends Object
	{
		static public function get NOTHING():String { return "nothing"; }
		static public function get NEW_CONN():String { return "new_conn"; }
		static public function get DEL_CONN():String { return "del_conn"; }
		public var state:String = NOTHING;
	}
}
