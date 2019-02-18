package io.github.mikiya83.github_parse_stats;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object binded to GitHub API JSON
 * 
 * @author Mikiya83
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetData {

	private int download_count;

	private String name;

	public int getDownload_count() {
		return download_count;
	}

	public String getName() {
		return name;
	}

	public void setDownload_count(int download_count) {
		this.download_count = download_count;
	}

	public void setName(String url) {
		this.name = url;
	}
}
