package com.ubertob.kondor.json

import com.ubertob.kondor.*
import com.ubertob.kondor.json.jsonnode.*
import com.ubertob.kondor.json.parser.*
import com.ubertob.kondortools.expectSuccess
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.random.Random

class JsonParserTest {

    private fun tokenize(jsonStr: String) = KondorTokenizer.tokenize(jsonStr)

    @Test
    fun `parse Boolean`() {

        repeat(3) {

            val value = Random.nextBoolean()

            val jsonString = JsonNodeBoolean(value, NodePathRoot).render()

            val tokens = tokenize(jsonString)

            val node = tokens.onRoot().parseJsonNodeBoolean().expectSuccess()

            expectThat(node.value).isEqualTo(value)
            expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
        }
    }


    @Test
    fun `render decimal Num`() {
        val num = "123456789123456789.01234567890123456789"
        val value = BigDecimal(num)

        val jsonString = JsonNodeNumber(value, NodePathRoot).render()

        expectThat(jsonString).isEqualTo(num)
    }


    @Test
    fun `parse Num`() {

        repeat(10) {

            val value = Random.nextDouble().toBigDecimal()

            val jsonString = JsonNodeNumber(value, NodePathRoot).render()

            val tokens = tokenize(jsonString)

            val node = tokens.onRoot().parseJsonNodeNum().expectSuccess()

            expectThat(node.num).isEqualTo(value)
            expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
        }

        repeat(10) {

            val value = Random.nextLong().toBigDecimal()

            val jsonString = JsonNodeNumber(value, NodePathRoot).render()

            val tokens = tokenize(jsonString)

            val node = tokens.onRoot().parseJsonNodeNum().expectSuccess()

            expectThat(node.num).isEqualTo(value)
            expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
        }

        repeat(10) {

            val value = Random.nextLong().toBigDecimal().pow(10)

            val jsonString = JsonNodeNumber(value, NodePathRoot).render()

//            println("$value -> $jsonString")

            val tokens = tokenize(jsonString)

            val node = tokens.onRoot().parseJsonNodeNum().expectSuccess()

            expectThat(node.num).isEqualTo(value)
            expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
        }

        repeat(10) {

            val value = Random.nextDouble().toBigDecimal().pow(10)

            val jsonString = JsonNodeNumber(value, NodePathRoot).render()

//            println("$value -> $jsonString")

            val tokens = tokenize(jsonString)

            val node = tokens.onRoot().parseJsonNodeNum().expectSuccess()

            expectThat(node.num).isEqualTo(value)
            expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
        }

    }


    @Test
    fun `parse empty String`() {

        val value = ""

        val jsonString = JsonNodeString(value, NodePathRoot).render()

        val tokens = tokenize(jsonString)

        val node = tokens.onRoot().parseJsonNodeString().expectSuccess()

        expectThat(node.text).isEqualTo(value)
        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }

    @Test
    fun `parse quote String`() {

        val value = "\""

        val jsonString = JsonNodeString(value, NodePathRoot).render()

        val tokens = tokenize(jsonString)

        val node = tokens.onRoot().parseJsonNodeString().expectSuccess()

        expectThat(node.text).isEqualTo(value)
        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }

    @Test
    fun `parse String`() {

        repeat(1000) {
            val value = randomString(text, 0, 10)
            val jsonString = JsonNodeString(value, NodePathRoot).render()

//            println("$value -> $jsonString")

            val tokens = tokenize(jsonString)

            val node = tokens.onRoot().parseJsonNodeString().expectSuccess()

//            println("-> ${node.text}")

            expectThat(node.text).isEqualTo(value)
            expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
        }
    }

    @Test
    fun `parse unicode String`() {

        repeat(100) {
            val value = randomString(digits + latin1 + japanese, 0, 10)
            val jsonString = JsonNodeString(value, NodePathRoot).render()

//            println("$value -> $jsonString")

            val tokens = tokenize(jsonString)

            val node = tokens.onRoot().parseJsonNodeString().expectSuccess()

//            println("-> ${node.text}")

            expectThat(node.text).isEqualTo(value)
            expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
        }
    }

    @Test
    fun `parse Null`() {

        val jsonString = JsonNodeNull(NodePathRoot).render()

        val tokens = tokenize(jsonString)

        tokens.onRoot().parseJsonNodeNull().expectSuccess()

        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }


    @Test
    fun `parse array`() {

        val jsonString = """
            ["abc", null, "def"]
        """.trimIndent()

        val tokens = tokenize(jsonString)

        val nodes = tokens.onRoot().parseJsonNodeArray().expectSuccess()

        expectThat(nodes.values.count()).isEqualTo(3)
        expectThat(nodes.render()).isEqualTo("""["abc", "def"]""")
        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }

    @Test
    fun `parse empty array nested`() {

        val jsonString = "[[],[]]".trimIndent()

        val tokens = tokenize(jsonString)

        val nodes = tokens.onRoot().parseJsonNodeArray().expectSuccess()

        expectThat(nodes.render()).isEqualTo("[[], []]")
        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }

    @Test
    fun `parse an object`() {

        val jsonString = """
          {
            "id": 123,
            "name": "Ann"
          }
        """.trimIndent()

        val tokens = tokenize(jsonString)

        val nodes = tokens.onRoot().parseJsonNodeObject().expectSuccess()

        val expected = """{"id": 123, "name": "Ann"}"""
        expectThat(nodes.render()).isEqualTo(expected)
        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }

    @Test
    fun `parse empty object`() {

        val jsonString = "{}".trimIndent()

        val tokens = tokenize(jsonString)

        val nodes = tokens.onRoot().parseJsonNodeObject().expectSuccess()

        expectThat(nodes.render()).isEqualTo("{}")
        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }

    @Test
    fun `parse an object with nulls`() {

        val jsonString = """
          {
            "id": 123,
            "name": "Ann",
            "somethingelse": null
          }
        """.trimIndent()

        val tokens = tokenize(jsonString)

        val nodes = tokens.onRoot().parseJsonNodeObject().expectSuccess()

        expectThat(nodes.fieldMap.size).isEqualTo(3)

        val expected = """{"id": 123, "name": "Ann"}"""
        expectThat(nodes.render()).isEqualTo(expected)
        expectThat(lastPosRead(tokens)).isEqualTo(jsonString.length)
    }

    private fun lastPosRead(tokens: TokensStream): Int =
        when(val t = tokens.last()){
            is Separator -> t.pos
            is Value -> t.pos + t.text.length - 1
            null -> 0
        }

}