package com.iqiyi.android.dlna.sdk.mediarenderer;

public interface QPlayListener
{
	public void onSetNetworkReceived(String ssid, String key, String authalgo, String cipheralgo);

	public void onQPlayAuthReceived(String seed);

	public String onInsertTracksReceived(String QueueID, String StartingIndex, String TracksMetaData);

	public String onRemoveTracksReceived(String QueueID, String StartingIndex, String TracksMetaData);

	public String onGetTracksInfoReceived(String StartingIndex, String NumberOfTracks);

	public String onSetTracksInfoReceived(String QueueID, String StartingIndex, String NextIndex, String TracksMetaData);

	public String onGetTracksCountReceived();

	public String onGetMaxTracksReceived();

	public void onSetLyricReceived(String songID, String lyricType, String lyric);

	public String onGetLyricSupportTypeReceived();

	public String onRemoveAllTracksReceived(String queueID, String updateID);
}