package cn.dema.chunk;

import java.util.ArrayList;

import cn.dema.main.Utils;

public class StringChunk implements  Chunk {

	public byte[] type;
	public byte[] size;
	public byte[] strCount;
	public byte[] styleCount;
	public byte[] unknown;
	public byte[] strPoolOffset;
	public byte[] stylePoolOffset;
	public byte[] strOffsets;
	public byte[] styleOffsets;
	public byte[] strPool;
	public byte[] stylePool;

	public ArrayList<String> stringContentList;


	public byte[] getByte(ArrayList<String> strList){
		
		byte[] strB = getStrListByte(strList);
		
		byte[] src = new byte[0];
		
		src = Utils.addByte(src, type);
		src = Utils.addByte(src, size);
		src = Utils.addByte(src, Utils.int2Byte(strList.size()));
		src = Utils.addByte(src, styleCount);
		src = Utils.addByte(src, unknown);
		src = Utils.addByte(src, strPoolOffset);
		src = Utils.addByte(src, stylePoolOffset);
		
		byte[] strOffsets = new byte[0];
		ArrayList<String> convertList = convertStrList(strList);
		
		int len = 0;
		for(int i=0;i<convertList.size();i++){
			strOffsets = Utils.addByte(strOffsets, Utils.int2Byte(len));
			len += (convertList.get(i).length()+4);
		}
		
		src = Utils.addByte(src, strOffsets);
		
		int newStyleOffsets = src.length;
		
		src = Utils.addByte(src, styleOffsets);
		
		int newStringPools = src.length;
		
		src = Utils.addByte(src, strB);
		
		src = Utils.addByte(src, stylePool);

		if(styleOffsets != null && styleOffsets.length > 0){
			src = Utils.replaceBytes(src, Utils.int2Byte(newStyleOffsets), 28+strList.size()*4);
		}

		src = Utils.replaceBytes(src, Utils.int2Byte(newStringPools), 20);

		if(src.length %4 != 0){
			src = Utils.addByte(src, new byte[]{0,0});
		}

		src = Utils.replaceBytes(src, Utils.int2Byte(src.length), 4);
		
		return src;
	}

	@Override
	public byte[] getByte() {
		return getByte(stringContentList);
	}

	@Override
	public void updateIndex(ArrayList<String> src, ArrayList<String> aim) {

	}

	@Override
	public int getLen(){
		return type.length+size.length+strCount.length+styleCount.length+
				unknown.length+strPoolOffset.length+stylePoolOffset.length+
				strOffsets.length+styleOffsets.length+strPool.length+stylePool.length;
	}
	
	public static StringChunk createChunk(byte[] byteSrc, int stringChunkOffset){
		StringChunk chunk = new StringChunk();

		chunk.type = Utils.copyByte(byteSrc, 0+stringChunkOffset, 4);

		chunk.size = Utils.copyByte(byteSrc, 4+stringChunkOffset, 4);
		int chunkSize = Utils.byte2int(chunk.size);

		chunk.strCount = Utils.copyByte(byteSrc, 8+stringChunkOffset, 4);
		int chunkStringCount = Utils.byte2int(chunk.strCount);

		chunk.stringContentList = new ArrayList<String>(chunkStringCount);

		chunk.styleCount = Utils.copyByte(byteSrc, 12+stringChunkOffset, 4);
		int chunkStyleCount = Utils.byte2int(chunk.styleCount);

		chunk.unknown = Utils.copyByte(byteSrc, 16+stringChunkOffset, 4);

		chunk.strPoolOffset = Utils.copyByte(byteSrc, 20+stringChunkOffset, 4);

		chunk.stylePoolOffset = Utils.copyByte(byteSrc, 24+stringChunkOffset, 4);

		chunk.strOffsets = Utils.copyByte(byteSrc, 28+stringChunkOffset, 4*chunkStringCount);

		chunk.styleOffsets = Utils.copyByte(byteSrc, 28+stringChunkOffset+4*chunkStringCount, 4*chunkStyleCount);
		
		int stringContentStart = 8 + Utils.byte2int(chunk.strPoolOffset);
		
		byte[] chunkStringContentByte = Utils.copyByte(byteSrc, stringContentStart, chunkSize);

		byte[] firstStringSizeByte = Utils.copyByte(chunkStringContentByte, 0, 2);
		int firstStringSize = Utils.byte2Short(firstStringSizeByte)*2;
		byte[] firstStringContentByte = Utils.copyByte(chunkStringContentByte, 2, firstStringSize+2);
		
		String firstStringContent = new String(firstStringContentByte);
		chunk.stringContentList.add(Utils.filterStringNull(firstStringContent));
		int endStringIndex = 2+firstStringSize+2;
		while(chunk.stringContentList.size() < chunkStringCount){
			int stringSize = Utils.byte2Short(Utils.copyByte(chunkStringContentByte, endStringIndex, 2))*2;
			byte[] temp = Utils.copyByte(chunkStringContentByte, endStringIndex+2, stringSize+2);
			String str = new String(temp);
			chunk.stringContentList.add(Utils.filterStringNull(str));
			endStringIndex += (2+stringSize+2);
		}
		
		int len = 0;
		for(String str : chunk.stringContentList){
			len += 2;
			len += str.length()*2;
			len += 2;
		}
		chunk.strPool = Utils.copyByte(byteSrc, stringContentStart, len);
		int stylePool = stringContentStart + len;
		
		chunk.stylePool = Utils.copyByte(byteSrc, stylePool, chunkSize-(stylePool));
		
		return chunk;
	}
	
	private byte[] getStrListByte(ArrayList<String> strList){
		byte[] src = new byte[0];
		ArrayList<String> stringContentList = convertStrList(strList);
		for(int i=0;i<stringContentList.size();i++){
			byte[] tempAry = new byte[0];
			short len = (short)(stringContentList.get(i).length()/2);
			byte[] lenAry = Utils.shortToByte(len);
			tempAry = Utils.addByte(tempAry, lenAry);
			tempAry = Utils.addByte(tempAry, stringContentList.get(i).getBytes());
			tempAry = Utils.addByte(tempAry, new byte[]{0,0});
			src = Utils.addByte(src, tempAry);
		}
		return src;
	}
	
	private ArrayList<String> convertStrList(ArrayList<String> stringContentList){
		ArrayList<String> destList = new ArrayList<String>(stringContentList.size());
		for(String str : stringContentList){
			byte[] temp = str.getBytes();
			byte[] src = new byte[temp.length*2];
			for(int i=0;i<temp.length;i++){
				src[i*2] = temp[i];
				src[i*2+1] = 0;
			}
			destList.add(new String(src));
		}
		return destList;
	}
	
}
