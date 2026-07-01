package com.raf.fieldops.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raf.fieldops.ui.theme.FieldOpsTheme
import com.raf.fieldops.util.ThemePreference

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColour: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = containerColour),
        modifier = modifier
            .fillMaxWidth()

            .heightIn(min = 48.dp)

            .semantics { contentDescription = "$text button" }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(
    name = "CustomButton — Primary (Enabled)",
    showBackground = true
)
@Composable
private fun CustomButtonPreviewEnabled() {
    FieldOpsTheme {
        CustomButton(
            text = "Sign In",
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "CustomButton — Primary (Disabled)",
    showBackground = true
)
@Composable
private fun CustomButtonPreviewDisabled() {
    FieldOpsTheme {
        CustomButton(
            text = "Create Job",
            onClick = {},
            enabled = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "CustomButton — Error Colour (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CustomButtonPreviewErrorDark() {
    FieldOpsTheme(
        themePreference = ThemePreference.Dark
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            CustomButton(
                text = "Delete Account",
                onClick = {},
                containerColour = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
