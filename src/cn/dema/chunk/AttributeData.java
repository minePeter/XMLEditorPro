package cn.dema.chunk;

import java.util.Arrays;
import java.util.ArrayList;
import cn.dema.main.ParserChunkUtils;
import cn.dema.main.Utils;
import cn.dema.main.AttributeType;

public class AttributeData implements Chunk {

	public byte[] nameSpaceUriB;
	public byte[] nameB;
	public byte[] valueStringB;
	public byte[] typeB;
	public byte[] dataB;

	public int type;
	public int data;
	public int offset;
	
	public static AttributeData createAttribute(byte[] src) {
		AttributeData data = new AttributeData();
		data.nameSpaceUriB = Utils.copyByte(src, 0, 4);
		data.nameB = Utils.copyByte(src, 4, 4);
		data.valueStringB = Utils.copyByte(src, 8, 4);
		data.typeB = Utils.copyByte(src, 12, 4);
		data.dataB = Utils.copyByte(src, 16, 4);
		data.type = Utils.byte2int(data.typeB) << 24;
		data.data = Utils.byte2int(data.dataB);
		return data;
	}

	@Override
	public int getLen() {
		return 20;
	}

	@Override
	public byte[] getByte() {
		byte[] bytes = new byte[getLen()];
		Utils.byteConcat(bytes, nameSpaceUriB, 0);
		Utils.byteConcat(bytes, nameB, 4);
		Utils.byteConcat(bytes, valueStringB, 8);
		Utils.byteConcat(bytes, typeB, 12);
		Utils.byteConcat(bytes, dataB, 16);
		return bytes;
	}
	
	public static AttributeData createAttribute(int uri, int name, int value, int type, int data) {
		AttributeData attribute = new AttributeData();
		attribute.nameSpaceUriB = Utils.int2Byte(uri);
		attribute.nameB = Utils.int2Byte(name);
		attribute.dataB = Utils.int2Byte(data);
		attribute.valueStringB = Utils.int2Byte(value);
		attribute.typeB = Utils.int2Byte(type);
		attribute.type = type;
		attribute.data = data;
		return attribute;
	}

	@Override
	public void updateIndex(ArrayList<String> src, ArrayList<String> aim) {
		int name = Utils.byte2int(nameB);
		if (name >= 0 && name < src.size()) {
			String n = src.get(name);
			name = aim.indexOf(n);
			nameB = Utils.int2Byte(name);
		}
		int nameSpaceUri = Utils.byte2int(nameSpaceUriB);
		if (nameSpaceUri >= 0 && nameSpaceUri < src.size()) {
			String n = src.get(nameSpaceUri);
			nameSpaceUri = aim.indexOf(n);
			nameSpaceUriB = Utils.int2Byte(nameSpaceUri);
		}
		if (stringAttrType(typeB)) {
			int valueString = Utils.byte2int(valueStringB);
			if (valueString >= 0 && valueString < src.size()) {
				String n = src.get(valueString);
				valueString = aim.indexOf(n);
				valueStringB = Utils.int2Byte(valueString);
			}

			int data = Utils.byte2int(dataB);
			if (data >= 0 && data < src.size()) {
				String n = src.get(data);
				data = aim.indexOf(n);
				dataB = Utils.int2Byte(data);
			}
		}
	}

	public String getNameSpaceUri() {
		int nameSpaceUri = Utils.byte2int(nameSpaceUriB);
		if(nameSpaceUri < 0) {
			return "";
		}
		return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(nameSpaceUri);
	}

	public String getName() {
		int name = Utils.byte2int(nameB);
		if(name < 0){
			return "";
		}
		return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(name);
	}

	public String getValueString() {
		int value = Utils.byte2int(valueStringB);
		if(value < 0 || value >= ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size()){
			return "";
		}
		return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(value);
	}

	public String getData(){
		int data = Utils.byte2int(dataB);
		if(data < 0 || data >= ParserChunkUtils.xmlStruct.stringChunk.stringContentList.size()) {
			return "";
		}
		return ParserChunkUtils.xmlStruct.stringChunk.stringContentList.get(data);
	}

	private boolean stringAttrType(byte[] type){
		return type[3] == AttributeType.ATTR_STRING;
	}

	@Override
	public String toString() {
		return "AttributeData{" +
				"nameSpaceUri=" + getNameSpaceUri() +
				", name=" + getName() +
				", valueString=" +  getValueString() +
				", type=" + Arrays.toString(typeB) +
				", data=" + Arrays.toString(dataB) +
				'}';
	}
}
