## Samples and License

This project supports using real piano sample WAV files placed in app/src/main/res/raw. The app looks for files named note{MIDI}.wav, e.g. note60.wav for middle C (C4), note61.wav, ... up to note83.wav for the included 24 keys.

I did not add any sample files to the repository by default. There are two ways to add samples so the app uses them:

1) Manual: Add WAV files to app/src/main/res/raw in the repository with filenames note60.wav ... note83.wav.
   - The app will automatically detect and use them. If samples are missing, it falls back to synthesized sine-wave tones.

2) CI automatic download: Set a repository secret named SAMPLE_ZIP_URL containing a direct URL to a ZIP file that includes WAV files named note60.wav...note83.wav (or similarly named files matching note{midi}.wav).
   - When present, the GitHub Actions workflow will download that ZIP during the build, extract any note*.wav files into app/src/main/res/raw, build the debug APK, and upload it as a workflow artifact.

IMPORTANT: Make sure any sample pack you use has a license that permits your intended use. The workflow does not bundle or endorse any particular sample source—if you want, I can help find a free/public-domain/CC0 sample pack and configure the workflow to download it.
