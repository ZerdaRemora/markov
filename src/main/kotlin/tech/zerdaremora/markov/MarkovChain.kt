package tech.zerdaremora.markov

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

class MarkovChain
{
    private val dictionary = HashMap<List<String>, List<String>>()
    private val sentenceStarters = ArrayList<List<String>>()

    fun createDictionary(inputs: List<String>): Map<List<String>, List<String>>
    {
        val words = ArrayList<String>()
        val sentences = ArrayList<String>()

        for (paragraph in inputs)
        {
            val splitParagraph = paragraph.split(Regex("\\."))
            sentences.addAll(splitParagraph)
        }

        createSentenceStarters(sentences)

        inputs
            .filterNot { it.isBlank() }
            .map { it.toLowerCase() }
            .map { it.replace(Regex("[^a-z0-9\\-'\\s]"), "") }
            .map { it.replace(Regex("\n"), " ") }
            .flatMap { it.split(" ") }
            .filterNot { it.isBlank() }
            .map { it.trim() }
            .toCollection(words)

        val tuple = LinkedList<String>()

        for (word in words)
        {
            if (word.isBlank() || word.contains("http", true))
                continue

            // Keep adding to the tuple until we have at least 2 words.
            // Two words in each tuple as the next word in the chain will be
            // determined by the previous two words.
            if (tuple.size < 2)
            {
                tuple.add(word)
                continue
            }

            // Clone the tuple to use as a key (avoid using a reference!)
            val dictKey: List<String> = tuple.toList()
            // If a word list for this tuple already exists, get it.
            val tupleWordList: MutableList<String> = dictionary.getOrDefault(dictKey, ArrayList()).toMutableList()

            // Add the current word to that tuple.
            tupleWordList.add(word)

            dictionary[dictKey] = tupleWordList

            // Remove the first word of the tuple, then add the current word to
            // the end of the tuple.
            // This creates a new tuple of the next group of two words.
            tuple.poll()
            tuple.add(word)
        }

        return dictionary
    }

    fun generateSentence(wordCount: Int): String
    {
        val rand = Random.Default

        val starters = sentenceStarters[rand.nextInt(sentenceStarters.size)]

        return sentenceBuilder(wordCount, starters)
    }

    fun generateSentence(wordCount: Int, startingWord: String): String
    {
        val starters = searchSingleWord(startingWord)

        if (starters.isEmpty())
            return ""

        return sentenceBuilder(wordCount, starters)
    }

    private fun createSentenceStarters(inputSentences: List<String>)
    {
        val sentences = ArrayList<String>()
        inputSentences
            .asSequence()
            .map { it.toLowerCase() }
            .map { it.replace(Regex("[^a-z0-9\\-'\\s]"), "") }
            .map { it.replace(Regex("\n"), " ") }
            .filterNot { it.isBlank() }
            .map { it.trim() }
            .toCollection(sentences)

        for (sentence in sentences)
        {
            val words = sentence.split(Regex("\\s"))

            if (words.size > 2)
                sentenceStarters.add(listOf(words[0], words[1]))
        }
    }

    private fun sentenceBuilder(wordCount: Int, sentenceStarters: List<String>): String
    {
        val sb = StringBuilder()
        val rand = Random.Default
        val previousWords = sentenceStarters.toMutableList()

        sb.append(sentenceStarters[0]).append(" ").append(sentenceStarters[1])

        for (i in 0 until wordCount - 2)
        {
            val possibleWords = dictionary[previousWords]

            if (possibleWords == null)
            {
                val newTuple = searchSingleWord(previousWords[1])

                // If still no match, just skip.
                // TODO: Maybe choose completely random word.
                if (newTuple.isEmpty())
                    continue

                sb.append(" ").append(newTuple[1])

                previousWords.removeAt(0)
                previousWords.add(newTuple[1])

                continue
            }

            val nextWord = possibleWords[rand.nextInt(possibleWords.size)]
            sb.append(" ").append(nextWord)

            previousWords.removeAt(0)
            previousWords.add(nextWord)
        }

        sb.append(". tx")

        return sb.toString()
    }

    private fun searchSingleWord(word: String): List<String>
    {
        for (tuple in dictionary.keys)
        {
            if (tuple[0] == word)
                return tuple
        }

        for (tuple in dictionary.keys)
        {
            if (tuple[1] == word)
            {
                val rand = Random.Default
                val wordIndex = rand.nextInt(dictionary.getValue(tuple).size)

                return listOf(word, dictionary.getValue(tuple)[wordIndex])
            }
        }

        return emptyList()
    }
}