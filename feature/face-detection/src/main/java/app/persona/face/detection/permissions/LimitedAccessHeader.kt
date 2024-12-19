package app.persona.face.detection.permissions

import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.persona.components.MessageBox
import app.persona.theme.Dimens

@Composable
fun LimitedAccessHeader(
    onSelectMore: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Call onSelectMore when photo selection is completed
        onSelectMore()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MessageBox(text = "Limited photo access granted. You can select more photos anytime.")
        Spacer(modifier = Modifier.height(Dimens.Spacing08))

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val intent = Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                        type = "image/*"
                        putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 100)
                    }
                    photoPickerLauncher.launch(intent)
                }
            }
        ) {
            Text(
                text = "Select More Photos",
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.Spacing08))
    }
} 