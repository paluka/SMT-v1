package vialab.simpleMultiTouch;

import java.util.HashMap;
import java.util.Vector;

public class ZonePicker {
	Vector<Zone> zoneList = TouchClient.zoneList;//new ArrayList<Zone>();
	
	private HashMap<Long, Zone> idToZoneMap = new HashMap<Long, Zone>();

	public Zone pick(long id, int xScreen, int yScreen, Touch t, boolean mapID) {
		if (idToZoneMap.containsKey(id)) {
			return idToZoneMap.get(id);
		}
		
		if(mapID){
			// check zones **layers matter--last zone created is on top (wins)
			for (int i = zoneList.size() - 1; i >= 0; i--) {
				Zone zone = zoneList.get(i);
				if (zone != null && zone.active && zone.contains(xScreen, yScreen)) {
					idToZoneMap.put(id, zone);
					t.setZone(zone);
					return zone;
				}
			}
		}
		return null;
	}

	/*public void add(int index, Zone element) {
		zoneList.add(index, element);
	}

	public boolean add(Zone e) {
		return zoneList.add(e);
	}

	public boolean addAll(Collection<? extends Zone> c) {
		return zoneList.addAll(c);
	}

	public void clear() {
		zoneList.clear();
	}

	public Zone get(int index) {
		return zoneList.get(index);
	}

	public boolean remove(Zone o) {
		return zoneList.remove(o);
	}

	public int size() {
		return zoneList.size();
	}*/
	
	public void removeMapping(Zone zone){
		//if(idToZoneMap.containsValue(zone)){
			idToZoneMap.remove(zone);
		//}
	}
	
	public void removeMapping(long id){
		//if(idToZoneMap.containsValue(id)){
			idToZoneMap.remove(id);
		//}
	}

}
