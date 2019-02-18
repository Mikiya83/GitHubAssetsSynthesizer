package io.github.mikiya83.github_parse_stats;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Object binded to GitHub API JSON
 * 
 * @author Mikiya83
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubData {

	private List<AssetData> assets;

	private String name;

	private Instant published_at;

	public List<AssetData> getAssets() {
		return assets;
	}

	public String getName() {
		return name;
	}

	public Instant getPublished_at() {
		return published_at;
	}

	public void setAssets(List<AssetData> assets) {
		this.assets = assets;
	}

	public void setName(String url) {
		this.name = url;
	}

	public void setPublished_at(Instant published_at) {
		this.published_at = published_at;
	}
}
