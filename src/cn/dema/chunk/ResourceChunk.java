package cn.dema.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import cn.dema.main.Utils;

public class ResourceChunk implements Chunk {
	
	public byte[] type;
	public byte[] size;
	public byte[] ids;
	
	public ArrayList<Integer> resourcIdList;
	
	public static ResourceChunk createChunk(byte[] byteSrc, int offset){
		
		ResourceChunk chunk = new ResourceChunk();
		
		chunk.type = Utils.copyByte(byteSrc, 0+offset, 4);
		
		chunk.size = Utils.copyByte(byteSrc, 4+offset, 4);
		int chunkSize = Utils.byte2int(chunk.size);
		chunk.ids = Utils.copyByte(byteSrc, 8+offset, chunkSize-8);

		byte[] resourceIdByte = Utils.copyByte(byteSrc, 8+offset, chunkSize-8);
		chunk.resourcIdList = new ArrayList<Integer>(resourceIdByte.length/4);
		for(int i = 0; i < resourceIdByte.length; i += 4){
			int resId = Utils.byte2int(Utils.copyByte(resourceIdByte, i, 4));
			chunk.resourcIdList.add(resId);
		}
		return chunk;
	}

	private static final int[] resIds = {16842755, 16842766, 16842768, 16842776};

	public int insertProvider(){

		int ret = 0;
		for (int id : resIds) {
			if (!resourcIdList.contains(id)) {
				ret += 4;
				resourcIdList.add(id);
			}
		}
		Collections.sort(resourcIdList);
		int chunkSize = resourcIdList.size() * 4 + 8;
		size = Utils.int2Byte(chunkSize);
		
		ids = new byte[resourcIdList.size() * 4];
		for (int i = 0; i < resourcIdList.size(); i ++) {
			byte[] res = Utils.int2Byte(resourcIdList.get(i));
			for (int j = 0; j < 4; j ++) {
				ids[i * 4 + j] = res[j];
			}
		}
		return ret;
	}

	@Override
	public byte[] getByte(){
		byte[] bytes = new byte[getLen()];
		bytes = Utils.byteConcat(bytes, type, 0);
		bytes = Utils.byteConcat(bytes, size, 4);
		bytes = Utils.byteConcat(bytes, ids, 8);
		return bytes;
	}

	@Override
	public int getLen(){
		return 8 + ids.length;
	}

	@Override
	public void updateIndex(ArrayList<String> src, ArrayList<String> aim) {
		
	}

	@Override
	public String toString() {
		return "ResourceChunk{" +
				"type=" + Arrays.toString(type) +
				", size=" + Arrays.toString(size) +
				", ids=" + Arrays.toString(ids) +
				", resourcIdList=" + resourcIdList +
				'}';
	}
}
