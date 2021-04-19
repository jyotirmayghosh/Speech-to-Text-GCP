import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import java.nio.file.Files

import java.nio.file.Paths
import java.io.IOException

import com.google.auth.oauth2.ServiceAccountCredentials

import com.google.auth.oauth2.GoogleCredentials
import com.google.common.io.Resources.getResource
import java.io.File
import java.io.InputStream
import java.time.LocalTime
import java.time.temporal.ChronoUnit


private val rootDir = "src/main"
private var projectId: String? = null
private var speechClient: SpeechClient? = null

private var startTime: LocalTime? = null
private var endTime: LocalTime? = null

fun main() {
    try {
        //Get credentials:
        val myCredentials = GoogleCredentials.fromStream(readFileAsInputStream("$rootDir/resources/YOUR_FILE_NAME.json"))
        projectId = (myCredentials as ServiceAccountCredentials).projectId

        val speechSettings = SpeechSettings.newBuilder().setCredentialsProvider { myCredentials }.build()

        //Set credentials and get translate service:
        speechClient = SpeechClient.create(speechSettings)
    } catch (ioe: IOException) {
        ioe.printStackTrace()
    }
    sampleRecognize()
}

fun readFileAsInputStream(fileName: String): InputStream{
    val path = Paths.get(fileName)
    return File(path.toUri()).inputStream()
}

fun sampleRecognize() {
    startTime = LocalTime.now()
    try {
        val localFilePath = "$rootDir/resources/voice4.wav"

        // The language of the supplied audio
        val languageCode = "en-US"//"hi-IN"

        // Sample rate in Hertz of the audio data sent
        val sampleRateHertz = 44100

        // Encoding of audio data sent. This sample sets this explicitly.
        // This field is optional for FLAC and WAV audio formats.
        val encoding: RecognitionConfig.AudioEncoding = RecognitionConfig.AudioEncoding.LINEAR16
        val config: RecognitionConfig = RecognitionConfig.newBuilder()
            .setLanguageCode(languageCode)
            .setSampleRateHertz(sampleRateHertz)
            .setEncoding(encoding)
            .build()

        val path = Paths.get(localFilePath)
        val data = Files.readAllBytes(path)
        val content: ByteString = ByteString.copyFrom(data)
        val audio: RecognitionAudio = RecognitionAudio.newBuilder().setContent(content).build()

        val request: RecognizeRequest = RecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build()

        val response: RecognizeResponse = speechClient!!.recognize(request)
        for (result in response.resultsList) {
            // First alternative is the most probable result
            val alternative: SpeechRecognitionAlternative = result.alternativesList[0]
            println("Transcript: ${alternative.transcript}")
        }
        endTime = LocalTime.now()
        println("Time taken: ${ChronoUnit.SECONDS.between(startTime, endTime)}")
    } catch (exception: Exception) {
        System.err.println("Failed to create the client due to: $exception")
    }
}