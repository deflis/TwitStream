package net.deflis.android.task;

import java.io.IOException;
import java.io.OutputStream;

class SizeCountOutputStream extends OutputStream {
	OutputStream mOutputStream;
	Long mCount = 0L;

	public SizeCountOutputStream(OutputStream os) {
		this.mOutputStream = os;
	}

	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		mOutputStream.write(buffer, offset, count); 
		mCount += count;
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		mOutputStream.write(buffer);
		mCount += buffer.length;
	}

	@Override
	public void write(int oneByte) throws IOException {
		mOutputStream.write(oneByte);
		mCount++;
	}

	@Override
	public void close() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		mOutputStream.close();
	}

	@Override
	public void flush() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		mOutputStream.flush();
	}

	public Long getSize(){
		return mCount;
	}
}
