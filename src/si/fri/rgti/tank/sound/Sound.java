package si.fri.rgti.tank.sound;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.util.WaveData;

public class Sound {
	private static final String[] soundFiles = new String[] {
			"sounds/expl.wav", "sounds/fire.wav", "sounds/tankup2.wav", "sounds/idle.wav"  };

	/** Sound indices */
	public static final int EXPLOSION = 0;
	public static final int FIRE = 1;
	public static final int TANKUP = 2;
	public static final int IDLE = 3;

	/** Buffers */
	static IntBuffer buffer = BufferUtils.createIntBuffer(soundFiles.length);
	static IntBuffer source = BufferUtils.createIntBuffer(soundFiles.length);

	public static void init() {
		try {
			AL.create(null, 15, 22050, true);
		} catch (LWJGLException le) {
			le.printStackTrace();
			return;
		}
		AL10.alGetError();

		// Load the wav data.
		if (loadALData() == AL10.AL_FALSE) {
			System.out.println("Error loading data.");
			return;
		}
		
		AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE);
	}

	private static int loadALData() {
		// Load wav data into a buffers.
		AL10.alGenBuffers(buffer);
		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			return AL10.AL_FALSE;

		WaveData waveFile = null;
		for (int i = 0; i < soundFiles.length; i++) {

			try {
				waveFile = WaveData.create(new BufferedInputStream(
						new FileInputStream(soundFiles[i])));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println(soundFiles[i]);
			}
			System.out.println(waveFile);
			AL10.alBufferData(buffer.get(i), waveFile.format, waveFile.data,
					waveFile.samplerate);
			waveFile.dispose();
		}

		// Bind buffers into audio sources.
		AL10.alGenSources(source);
		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			return AL10.AL_FALSE;

		for (int i = 0; i < soundFiles.length; i++) {
			AL10.alSourcei(source.get(i), AL10.AL_BUFFER, buffer.get(i));
			AL10.alSourcef(source.get(i), AL10.AL_PITCH, 1.0f);
			AL10.alSourcef(source.get(i), AL10.AL_GAIN, 1.0f);
			
			if(i==TANKUP || i==IDLE)
				AL10.alSourcei(source.get(i), AL10.AL_LOOPING, AL10.AL_TRUE );
		}

		if (AL10.alGetError() == AL10.AL_NO_ERROR)
			return AL10.AL_TRUE;
		return AL10.AL_FALSE;
	}

	public static void playASound(int sound, Vector3f position) {
		AL10.alSource3f(source.get(sound), AL10.AL_POSITION, position.x, position.y, position.z);
		AL10.alSourcePlay(source.get(sound));
	}
	
	public static void stopSound(int sound){
		AL10.alSourceStop(source.get(sound));
	}
	
	public static void update(Vector3f listenerPos){
		if(listenerPos==null) return;
		AL10.alListener3f(AL10.AL_POSITION, listenerPos.x, listenerPos.y, listenerPos.z);
	}
}
