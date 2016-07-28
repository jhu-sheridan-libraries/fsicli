package fsi.fsicli;

public class ImageInfo {
	private final String path;
	private final int width;
	private final int height;
	
	public ImageInfo(String path, int width, int height) {
		this.path = path;
		this.width = width;
		this.height = height;
	}

	public String getPath() {
		return path;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	

	@Override
	public String toString() {
		return "ImageInfo [path=" + path + ", width=" + width + ", height=" + height + "]";
	}
}
