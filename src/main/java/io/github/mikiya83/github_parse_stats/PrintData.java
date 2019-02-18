package io.github.mikiya83.github_parse_stats;

/**
 * Object to print results of data computing.
 * 
 * @author Mikiya83
 *
 */
public class PrintData {

	private long age;

	private String assetDate;

	private String assetFileName;

	private int downloads;

	private double downStat;

	private String name;

	private String state;

	public PrintData(String name, String assetDate, String assetFileName, int downloads, long age, String state,
			double downStat) {
		super();
		this.name = name;
		this.assetDate = assetDate;
		this.assetFileName = assetFileName;
		this.downloads = downloads;
		this.age = age;
		this.state = state;
		this.downStat = downStat;
	}

	public long getAge() {
		return age;
	}

	public String getAssetDate() {
		return assetDate;
	}

	public String getAssetFileName() {
		return assetFileName;
	}

	public int getDownloads() {
		return downloads;
	}

	public double getDownStat() {
		return downStat;
	}

	public String getName() {
		return name;
	}

	public String getState() {
		return state;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public void setAssetDate(String assetDate) {
		this.assetDate = assetDate;
	}

	public void setAssetFileName(String assetFileName) {
		this.assetFileName = assetFileName;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}

	public void setDownStat(double downStat) {
		this.downStat = downStat;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setState(String state) {
		this.state = state;
	}

}
