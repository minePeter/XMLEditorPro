package cn.dema.chunk;

import java.util.ArrayList;
import java.util.Arrays;

import cn.dema.main.ChunkTypeNumber;
import cn.dema.main.Utils;

public class EndTagChunk implements Chunk {
	
	public byte[] type = new byte[4];
	public byte[] size = new byte[4];
	public byte[] lineNumber = new byte[4];
	public byte[] unknown = new byte[4];
	public byte[] uri = new byte[4];
	public byte[] name = new byte[4];
	
	public int offset;
	public String tagValue;
	
	public EndTagChunk(){
		type = Utils.int2Byte(ChunkTypeNumber.CHUNK_ENDTAG);
		size = Utils.int2Byte(24);
		lineNumber = new byte[4];
		unknown = new byte[4];
		uri = Utils.int2Byte(-1);		
	}
	
	public static EndTagChunk createChunk(int name){
		EndTagChunk chunk = new EndTagChunk();
		chunk.name = Utils.int2Byte(name);
		return chunk;
	}

	public static EndTagChunk createChunk(byte[] byteSrc, int offset){
		EndTagChunk chunk = new EndTagChunk();
		chunk.offset = offset;
		chunk.type = Utils.copyByte(byteSrc, 0, 4);
		chunk.size = Utils.copyByte(byteSrc, 4, 4);
		chunk.lineNumber = Utils.copyByte(byteSrc, 8, 4);
		chunk.unknown = Utils.copyByte(byteSrc, 12, 4);
		chunk.uri = Utils.copyByte(byteSrc, 16, 4);
		chunk.name = Utils.copyByte(byteSrc, 20, 4);
		return chunk;
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
		return bytes;
	}
	
	@Override
	public int getLen(){
		return 24;
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
	}

	@Override
	public String toString() {
		return "EndTagChunk{" +
				"size=" + Arrays.toString(size) +
				", name=" + Arrays.toString(name) +
				", tagValue='" + tagValue + '\'' +
				'}';
	}
}
