package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ActionType
import com.example.data.repository.PriyaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Priya AI", appName)
  }

  @Test
  fun `verify nlp app launcher parsing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = PriyaRepository(context)
    val contacts = repo.getContacts()

    val parsed = repo.parseCommand("open whatsapp", contacts)
    assertEquals(ActionType.OPEN_APP, parsed.actionType)
    assertEquals("com.whatsapp", parsed.targetData)
  }

  @Test
  fun `verify nlp call mummy parsing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = PriyaRepository(context)
    val contacts = repo.getContacts()

    val parsed = repo.parseCommand("mummy ko call lagao", contacts)
    assertEquals(ActionType.CALL_CONTACT, parsed.actionType)
    assertNotNull(parsed.targetData)
  }
}
