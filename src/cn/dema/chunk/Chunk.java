package cn.dema.chunk;

import java.util.ArrayList;

public interface Chunk {

	int getLen();

	byte[] getByte();

	void updateIndex(ArrayList<String> src, ArrayList<String> aim);

}
