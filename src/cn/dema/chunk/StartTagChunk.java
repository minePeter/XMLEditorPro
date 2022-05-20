package cn.dema.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import cn.dema.main.ChunkTypeNumber;
import cn.dema.main.Utils;
import cn.dema.main.ParserChunkUtils;

public class StartTagChunk implements Chunk {

	public byte[] type;
	public byte[] size;
	public byte[] lineNumber;
	public byte[] unknown;
	public byte[] uri;
	public byte[] name;
	public byte[] flag;
	public byte[] attCount;
	public byte[] classAttr;
	public byte[] attribute;

	public ArrayList<AttributeData> attrList;
	
	public int offset;
	
	public StartTagChunk(){
		type = Utils.int2Byte(ChunkTypeNumber.CHUNK_STARTTAG);
		lineNumber = new byte[4];
		unknown = Utils.int2Byte(-1);
		flag = new byte[4];
		classAttr = new byte[4];

		int flatInt = 0;
		flatInt = flatInt | 0x00140014;
		flag = Utils.int2Byte(flatInt);
	}

	@Override
	public byte[] getByte(){
		byte[] bytes = new byte[getLen()];
		bytes = Utils.byteConcat(bytes, type, 0);
		bytes = Utils.byteConcat(bytes, size, 4);
		bytes = Utils.byteConcat(bytes, lineNumber, 8);
		bytes = Utils.byteConcat(bytes, unknown, 12);
		bytes = Utils.byteConcat(bytes, uri, 16);
		bytes = Utils.byteConcat(bytes, name, 20);
		bytes = Utils.byteConcat(bytes, flag, 24);
		bytes = Utils.byteConcat(bytes, attCount, 28);
		bytes = Utils.byteConcat(bytes, classAttr, 32);
		bytes = Utils.byteConcat(bytes, attribute, 36);
		return bytes;
	}

	@Override
	public int getLen(){
		return 36 + (attribute == null ? 0 : attribute.length);
	}

	public void addAttribute(AttributeData data) {
		attrList.add(data);
		attCount = Utils.int2Byte(attrList.size());
		size = Utils.int2Byte(getLen());
	}

	public void removeAttribute(AttributeData data) {
		attrList.remove(data);
		attCount = Utils.int2Byte(attrList.size());
		size = Utils.int2Byte(getLen());
	}
	
	public static StartTagChunk createChunk(int name, int attCount, int uri, ArrayList<AttributeData> list){
		StartTagChunk chunk = new StartTagChunk();
		chunk.size = new byte[4];
		chunk.name = Utils.int2Byte(name);
		chunk.uri = Utils.int2Byte(uri);
		chunk.attCount = Utils.int2Byte(attCount);
		chunk.attribute = new byte[attCount * 20];
		chunk.size = Utils.int2Byte(chunk.getLen());

		chunk.attrList = list;
		int offset = 0;
		if (chunk.attrList != null) {
			for (AttributeData data : chunk.attrList) {
				byte[] attribute = data.getByte();
				System.arraycopy(attribute, 0, chunk.attribute, offset, attribute.length);
				offset += attribute.length;
			}
		}

		return chunk;
	}
	
	public static StartTagChunk createChunk(byte[] byteSrc, int offset){
		StartTagChunk chunk = new StartTagChunk();
		chunk.offset = offset;
		chunk.type = Utils.copyByte(byteSrc, 0, 4);
		chunk.size = Utils.copyByte(byteSrc, 4, 4);
		chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);
		chunk.unknown = Utils.copyByte(byteSrc, 12, 4);
		chunk.uri = Utils.copyByte(byteSrc, 16, 4);
		chunk.name = Utils.copyByte(byteSrc, 20, 4);
		chunk.flag = Utils.copyByte(byteSrc, 24, 4);
		chunk.attCount = Utils.copyByte(byteSrc, 28, 4);
		int attrCount = Utils.byte2int(chunk.attCount);
		chunk.classAttr = Utils.copyByte(byteSrc, 32, 4);
		chunk.attribute = Utils.copyByte(byteSrc, 36, attrCount*20);
		chunk.attrList = new ArrayList<AttributeData>(attrCount);

		for(int i=0;i<attrCount;i++){
			AttributeData attrData = new AttributeData();
			for(int j=0;j<5;j++){
				byte[] value = Utils.copyByte(byteSrc, 36+i*20+j*4, 4);
				attrData.offset = offset + 36 + i*20;
				switch(j){
					case 0:
						attrData.nameSpaceUriB = value;
						break;
					case 1:
						attrData.nameB = value;
						break;
					case 2:
						attrData.valueStringB = value;
						break;
					case 3:
						attrData.typeB = value;
						break;
					case 4:
						attrData.dataB = value;
						break;
				}
			}
			chunk.attrList.add(attrData);
		}
		
		return chunk;
	}

	@Override
	public void updateIndex(ArrayList<String> src, ArrayList<String> aim) {
		int nameIndex = Utils.byte2int(name);
		if (nameIndex >= 0 && nameIndex < src.size()) {
			String n = src.get(nameIndex);
			nameIndex = aim.indexOf(n);
			name = Utils.int2Byte(nameIndex);
		}

		int uriIndex = Utils.byte2int(uri);
		if (uriIndex >= 0 && uriIndex < src.size()) {
			String n = src.get(uriIndex);
			uriIndex = aim.indexOf(n);
			uri = Utils.int2Byte(uriIndex);
		}

		int offset = 0;
		if (attrList != null) {
			for (AttributeData data : attrList) {
				data.updateIndex(src, aim);
				byte[] bb = data.getByte();
				System.arraycopy(bb, 0, attribute, offset, bb.length);
				offset += bb.length;
			}
		}
	}

	public String getName() {
		int index = Utils.byte2int(name);
		if (index < 0 || index >= ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size()) {
			return "";
		}
		return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(index);
	}

	@Override
	public String toString() {
		return "StartTagChunk{" +
				"type=" + Arrays.toString(type) +
				", size=" + Arrays.toString(size) +
				", lineNumber=" + Arrays.toString(lineNumber) +
				", unknown=" + Arrays.toString(unknown) +
				", uri=" + Arrays.toString(uri) +
				", name=" + Arrays.toString(name) +
				", flag=" + Arrays.toString(flag) +
				", attCount=" + Arrays.toString(attCount) +
				", classAttr=" + Arrays.toString(classAttr) +
				", attribute=" + Arrays.toString(attribute) +
				", attrList=" + attrList +
				", offset=" + offset +
				'}';
	}
}
