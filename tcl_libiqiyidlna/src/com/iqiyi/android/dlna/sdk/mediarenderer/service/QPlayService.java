package com.iqiyi.android.dlna.sdk.mediarenderer.service;

import org.cybergarage.util.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.*;
import org.cybergarage.upnp.device.InvalidDescriptionException;

import com.iqiyi.android.dlna.sdk.mediarenderer.MediaRenderer;
import com.iqiyi.android.dlna.sdk.mediarenderer.QPlayListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.QPlayServiceConstStr;

public class QPlayService extends Service implements ActionListener, QueryListener, ServiceInterface
{
	private static final String MID = "62900080";
	private static final String DID = "Q-1";
	private static final byte[] PSK = new byte[]
	{ (byte) 0x53, (byte) 0x8C, (byte) 0xEB, (byte) 0xAB, (byte) 0x08, (byte) 0xE4, (byte) 0x4D, (byte) 0x47, (byte) 0xA3,
			(byte) 0x60, (byte) 0xD2, (byte) 0x44, (byte) 0x5A, (byte) 0xF3, (byte) 0x28, (byte) 0xDE };

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	public QPlayService(MediaRenderer render)
	{
		setMediaRenderer(render);
		initService();
		setActionListener(this);
	}

	// //////////////////////////////////////////////
	// MediaRender
	// //////////////////////////////////////////////

	private MediaRenderer mediaRenderer;

	private void setMediaRenderer(MediaRenderer render)
	{
		mediaRenderer = render;
	}

	public MediaRenderer getMediaRenderer()
	{
		return mediaRenderer;
	}

	// //////////////////////////////////////////////
	// ActionListener
	// //////////////////////////////////////////////

	public boolean actionControlReceived(Action action)
	{
		boolean isActionSuccess = false;
		String actionName = action.getName();
		if (actionName == null)
		{
			Debug.message("[Error] QPlay action: actionName == null");
			action.setStatus(UPnPStatus.INVALID_ACTION);
			return false;
		}

		Debug.message("Receive QPlay action: " + actionName);
		MediaRenderer dmr = getMediaRenderer();
		QPlayListener listener = null;
		if (dmr != null)
		{
			listener = dmr.getQPlayListener();
		}

		if (actionName.equals(QPlayServiceConstStr.SetNetwork) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String SSID = action.getArgument(QPlayServiceConstStr.SSID).getValue();
			String Key = action.getArgument(QPlayServiceConstStr.Key).getValue();
			String AuthAlgo = action.getArgument(QPlayServiceConstStr.AuthAlgo).getValue();
			String CipherAlgo = action.getArgument(QPlayServiceConstStr.CipherAlgo).getValue();
			Debug.message("SSID=" + SSID + " Key=" + Key + " AuthAlgo=" + AuthAlgo + " CipherAlgo=" + CipherAlgo);
			if (listener != null)
			{
				listener.onSetNetworkReceived(SSID, Key, AuthAlgo, CipherAlgo);
			}

			isActionSuccess = true;
		} else if (actionName.equals(QPlayServiceConstStr.QPlayAuth) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String Seed = action.getArgument(QPlayServiceConstStr.Seed).getValue();
			Debug.message("Seed = " + Seed);
			if (listener != null)
			{
				listener.onQPlayAuthReceived(Seed);
			}

			// Product Names: Q-1
			// MID: 62900080
			// PSK: 0x53, 0x8C, 0xEB, 0xAB, 0x08, 0xE4, 0x4D, 0x47, 0xA3, 0x60, 0xD2, 0x44, 0x5A, 0xF3, 0x28, 0xDE
			// Code: MD5[Seed+ pre-shared key]
			byte[] temp = MD5Util.byteMerger(Seed.getBytes(), PSK);
			action.getArgument(QPlayServiceConstStr.Code).setValue(MD5Util.getMd5(temp, temp.length));
			action.getArgument(QPlayServiceConstStr.MID).setValue(MID);
			action.getArgument(QPlayServiceConstStr.DID).setValue(DID);

			isActionSuccess = true;
		} else if (actionName.equals(QPlayServiceConstStr.InsertTracks) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String QueueID = action.getArgument(QPlayServiceConstStr.QueueID).getValue();
			String StartingIndex = action.getArgument(QPlayServiceConstStr.StartingIndex).getValue();
			String TracksMetaData = action.getArgument(QPlayServiceConstStr.TracksMetaData).getValue();
			Debug.message("QueueID=" + QueueID + " StartingIndex=" + StartingIndex + " TracksMetaData=" + TracksMetaData);

			String NumberOfSuccess = "";
			if (listener != null)
			{
				NumberOfSuccess = listener.onInsertTracksReceived(QueueID, StartingIndex, TracksMetaData);
			}

			action.getArgument(QPlayServiceConstStr.NumberOfSuccess).setValue(NumberOfSuccess);

			isActionSuccess = true;
		} else if (actionName.equals(QPlayServiceConstStr.RemoveTracks) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String QueueID = action.getArgument(QPlayServiceConstStr.QueueID).getValue();
			String StartingIndex = action.getArgument(QPlayServiceConstStr.StartingIndex).getValue();
			String NumberOfTracks = action.getArgument(QPlayServiceConstStr.NumberOfTracks).getValue();
			Debug.message("QueueID=" + QueueID + " StartingIndex=" + StartingIndex + " NumberOfTracks=" + NumberOfTracks);

			String NumberOfSuccess = "";
			if (listener != null)
			{
				NumberOfSuccess = listener.onRemoveTracksReceived(QueueID, StartingIndex, NumberOfTracks);
			}

			action.getArgument(QPlayServiceConstStr.NumberOfSuccess).setValue(NumberOfSuccess);

			isActionSuccess = true;
		} else if (actionName.equals(QPlayServiceConstStr.GetTracksInfo) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String StartingIndex = action.getArgument(QPlayServiceConstStr.StartingIndex).getValue();
			String NumberOfTracks = action.getArgument(QPlayServiceConstStr.NumberOfTracks).getValue();
			Debug.message("StartingIndex=" + StartingIndex + " NumberOfTracks=" + NumberOfTracks);

			String TracksMetaData = "";
			if (listener != null)
			{
				TracksMetaData = listener.onGetTracksInfoReceived(StartingIndex, NumberOfTracks);
			}

			action.getArgument(QPlayServiceConstStr.TracksMetaData).setValue(TracksMetaData);

			isActionSuccess = true;
		} else if (actionName.equals(QPlayServiceConstStr.SetTracksInfo) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String QueueID = action.getArgument(QPlayServiceConstStr.QueueID).getValue();
			String StartingIndex = action.getArgument(QPlayServiceConstStr.StartingIndex).getValue();
			String NextIndex = action.getArgument(QPlayServiceConstStr.NextIndex).getValue();
			String TracksMetaData = action.getArgument(QPlayServiceConstStr.TracksMetaData).getValue();
			Debug.message("QueueID=" + QueueID + " StartingIndex=" + StartingIndex + " NextIndex=" + NextIndex
					+ " TracksMetaData=" + TracksMetaData);

			String NumberOfSuccess = "";
			if (listener != null)
			{
				NumberOfSuccess = listener.onSetTracksInfoReceived(QueueID, StartingIndex, NextIndex, TracksMetaData);
			}

			action.getArgument(QPlayServiceConstStr.NumberOfSuccess).setValue(NumberOfSuccess);

			isActionSuccess = true;
		} else if (actionName.equals(QPlayServiceConstStr.GetTracksCount) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String NrTracks = "";
			if (listener != null)
			{
				NrTracks = listener.onGetTracksCountReceived();
			}

			action.getArgument(QPlayServiceConstStr.NrTracks).setValue(NrTracks);

			isActionSuccess = true;
		} else if (actionName.equals(QPlayServiceConstStr.GetMaxTracks) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String MaxTracks = "";
			if (listener != null)
			{
				MaxTracks = listener.onGetMaxTracksReceived();
			}

			action.getArgument(QPlayServiceConstStr.MaxTracks).setValue(MaxTracks);

			isActionSuccess = true;
		}  else if (actionName.equals(QPlayServiceConstStr.GetLyricSupportType) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String LyricType = "LRC";
			if (listener != null)
			{
				LyricType = listener.onGetLyricSupportTypeReceived();
			}

			action.getArgument(QPlayServiceConstStr.LyricType).setValue(LyricType);

			isActionSuccess = true;
		}  else if (actionName.equals(QPlayServiceConstStr.SetLyric) == true)
		{
			Debug.message("Process QPlay action: " + actionName);

			String SongID = action.getArgument(QPlayServiceConstStr.SongID).getValue();
			String LyricType = action.getArgument(QPlayServiceConstStr.LyricType).getValue();
			String Lyric = action.getArgument(QPlayServiceConstStr.Lyric).getValue();
			Debug.message("SongID=" + SongID + " LyricType=" + LyricType + " Lyric=" + Lyric);

			if (listener != null)
			{
				listener.onSetLyricReceived(SongID, LyricType, Lyric);
			}

			isActionSuccess = true;
		} else if(actionName.equals(QPlayServiceConstStr.RemoveAllTracks) == true)
		{
			Debug.message("Process QPlay action: " + actionName);
			String QueueID = action.getArgument(QPlayServiceConstStr.QueueID).getValue();
			String UpdateID = action.getArgument(QPlayServiceConstStr.UpdateID).getValue();
			Debug.message("QueueID=" + QueueID + " UpdateID=" + UpdateID);

			if (listener != null)
			{
				listener.onRemoveAllTracksReceived(QueueID, UpdateID);
			}

			isActionSuccess = true;
		} else
		{
			Debug.message("[Error] Unknown QPlay Action: " + actionName);
			action.setStatus(UPnPStatus.INVALID_ACTION);
		}

		return isActionSuccess;
	}

	public boolean queryControlReceived(StateVariable stateVar)
	{
		return false;
	}

	@Override
	public void initService()
	{
		// 设置服务的描述地址，控制地址，订阅地址
		setServiceType(QPlayServiceConstStr.SERVICE_TYPE);
		setServiceID(QPlayServiceConstStr.SERVICE_ID);
		setControlURL(QPlayServiceConstStr.CONTROL_URL);
		setSCPDURL(QPlayServiceConstStr.SCPDURL);
		setEventSubURL(QPlayServiceConstStr.EVENTSUB_URL);

		try
		{
			loadSCPD(QPlayServiceConstStr.SCPD);
		} catch (InvalidDescriptionException e)
		{
			e.printStackTrace();
		}
	}
}
