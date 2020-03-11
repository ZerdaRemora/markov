package tech.zerdaremora.markov

import java.io.File
import kotlin.random.Random

fun main()
{
    val inputText = ArrayList<String>()
    val markov = MarkovChain()
    inputText.add(File("test.txt").readText())

    val dict = markov.createDictionary(inputText)


    val rand = Random.Default
    for (i in 0..10)
    {
        val numberOfWords = rand.nextInt(16, 36)
        val sentence = markov.generateSentence(numberOfWords)

        println(sentence)
    }
}