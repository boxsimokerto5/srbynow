package com.example.data.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatReplyHelperTest {

    @Test
    fun `encode and parse embedded reply preserves Indonesian sender snippet and multiline body`() {
        val encoded = ChatReplyHelper.encodeTextWithReply(
            bodyText = "Alhamdulillah lancar cak\nSemoga lekas pulih!",
            replyToId = "msg_ivak_01",
            replyToSender = "ivakhudin",
            replyToSnippet = "anake sampeyan sakit apa ki?",
        )

        val parsed = ChatReplyHelper.parseEmbeddedReply(encoded)
        requireNotNull(parsed)
        assertEquals("msg_ivak_01", parsed.replyToId)
        assertEquals("ivakhudin", parsed.replyToSender)
        assertEquals("anake sampeyan sakit apa ki?", parsed.replyToSnippet)
        assertEquals("Alhamdulillah lancar cak\nSemoga lekas pulih!", parsed.cleanText)
    }

    @Test
    fun `regular message without reply marker returns null on parse and unchanged preview`() {
        val plain = "Halo warga Surabaya!"
        assertNull(ChatReplyHelper.parseEmbeddedReply(plain))
        assertEquals(plain, ChatReplyHelper.stripReplyMarkerForPreview(plain))
    }

    @Test
    fun `sanitizeSnippet formats photo messages cleanly and strips nested reply markers`() {
        val nested = ChatReplyHelper.encodeTextWithReply(
            bodyText = "Soto Cak Har enak pol",
            replyToId = "msg_01",
            replyToSender = "Eko wahyudi",
            replyToSnippet = "Rekomendasi soto rek",
        )
        assertEquals("Soto Cak Har enak pol", ChatReplyHelper.sanitizeSnippet(nested, hasPhoto = false))
        assertEquals("📷 Foto", ChatReplyHelper.sanitizeSnippet("", hasPhoto = true))
        assertEquals("📷 Lokasi warung", ChatReplyHelper.sanitizeSnippet("Lokasi warung", hasPhoto = true))
    }

    @Test
    fun `pocketbase schemas stay identical and include chat_messages reply columns`() {
        val rootSchema = File("pocketbase_collections.json")
        val assetsSchema = File("src/main/assets/pocketbase_collections.json")
        if (rootSchema.exists() && assetsSchema.exists()) {
            val rootContent = rootSchema.readText()
            val assetsContent = assetsSchema.readText()
            assertTrue(rootContent.contains("\"reply_to_id\""))
            assertTrue(rootContent.contains("\"reply_to_sender\""))
            assertTrue(rootContent.contains("\"reply_to_text\""))
            assertEquals(rootContent.trim(), assetsContent.trim())
        }
    }
}
