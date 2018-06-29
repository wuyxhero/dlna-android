package com.iqiyi.android.dlna.sdk.controlpoint;

import org.cybergarage.upnp.Device;

public interface IController
{
	/**
	 * Set the current device.
	 * 
	 * @param device
	 * @return
	 */
	void setDLNACurrentDevice(Device currentDevice);

	/**
	 * Play the video with the video path.
	 * 
	 * @param path
	 *            The path of the video.
	 * @return If is success to play the video.
	 */
	boolean play(String path, String title, MediaType type);

	/**
	 * Go on playing the video from the position.
	 * 
	 * @param pausePosition
	 *            The format must be 00:00:00.
	 */
	boolean goon(String pausePosition);

	/**
	 * All the state is "STOPPED" // "PLAYING" // "TRANSITIONING"// "PAUSED_PLAYBACK"// "PAUSED_RECORDING"// "RECORDING" //
	 * "NO_MEDIA_PRESENT//
	 */
	String getTransportState();

	/**
	 * Get the min volume value,this must be 0.
	 * 
	 */
	int getMinVolumeValue();

	/**
	 * Get the max volume value, usually it is 100.
	 * 
	 * @return The max volume value.
	 */
	int getMaxVolumeValue();

	/**
	 * Seek the playing video to a target position.
	 * 
	 * @param targetPosition
	 *            Target position we want to set.
	 * @return
	 */
	boolean seek(String targetPosition);

	/**
	 * Get the current playing position of the video.
	 * 
	 * @return Current playing position is 00:00:00
	 */
	String getPositionInfo();

	/**
	 * Get the duration of the video playing.
	 * 
	 * @return The media duration like 00:00:00,if get failed it will return null.
	 */
	String getMediaDuration();

	/**
	 * Mute the device or not.
	 * 
	 * @param targetValue
	 *            1 is that want mute, 0 if you want make it off of mute.
	 * @return If is success to mute the device.
	 */
	boolean setMute(String targetValue);

	/**
	 * Get if the device is mute.
	 * 
	 * @return 1 is mute, otherwise will return 0.
	 */
	String getMute();

	/**
	 * Set the device's voice.
	 * 
	 * @param value
	 *            Target voice want be set.
	 * @return
	 */
	boolean setVoice(int value);

	/**
	 * Get the current voice of the device.
	 * 
	 * @return Current voice.
	 */
	int getVoice();

	/**
	 * Stop to play.
	 * 
	 * @return If if success to stop the video.
	 */
	boolean stopplaying();

	/**
	 * Pause the playing video.
	 * 
	 * @return If if success to pause the video.
	 */
	boolean pause();
}
