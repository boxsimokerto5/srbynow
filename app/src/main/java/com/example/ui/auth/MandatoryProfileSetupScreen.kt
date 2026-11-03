package com.example.ui.auth

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.NearbyProfile
import com.example.data.model.UserAccount
import com.example.data.model.effectiveCitizenAvatarUrl
import com.example.data.model.effectiveCitizenDisplayName
import com.example.data.model.effectiveCitizenGender
import com.example.data.model.isValidCitizenDisplayName
import com.example.data.model.normalizeCitizenGender
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep

@Composable
fun MandatoryProfileSetupScreen(
    userAccount: UserAccount?,
    nearbyProfile: NearbyProfile?,
    userEmail: String,
    isSubmitting: Boolean,
    isScanningPhoto: Boolean,
    snackbarHostState: SnackbarHostState,
    onSubmitProfile: (
        displayName: String,
        gender: String,
        district: String,
        avatarUri: Uri?,
    ) -> Unit,
    onSignOut: () -> Unit,
) {
    // Non-dismissible gate: block back navigation until mandatory profile is saved
    BackHandler(enabled = true) { }

    val context = LocalContext.current
    val existingAvatarUrl = effectiveCitizenAvatarUrl(userAccount, nearbyProfile)
    val initialValidName = effectiveCitizenDisplayName(userAccount, nearbyProfile)
    val initialGender = effectiveCitizenGender(userAccount, nearbyProfile)
    val initialDistrict = userAccount?.district?.takeIf { it.isNotBlank() }
        ?: nearbyProfile?.district?.takeIf { it.isNotBlank() }
        ?: "Genteng"

    var nameInput by rememberSaveable { mutableStateOf(initialValidName) }
    var selectedGender by rememberSaveable { mutableStateOf(initialGender) }
    var selectedDistrict by rememberSaveable { mutableStateOf(initialDistrict) }
    var selectedAvatarUriString by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialValidName) {
        if (nameInput.isBlank() && initialValidName.isNotBlank()) {
            nameInput = initialValidName
        }
    }
    LaunchedEffect(initialGender) {
        if (selectedGender.isBlank() && initialGender.isNotBlank()) {
            selectedGender = initialGender
        }
    }

    val selectedAvatarUri = selectedAvatarUriString.takeIf { it.isNotBlank() }?.let(Uri::parse)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedAvatarUriString = uri.toString()
            }
        },
    )

    val isPhotoReady = selectedAvatarUri != null || existingAvatarUrl.isNotBlank()
    val isNameReady = isValidCitizenDisplayName(nameInput)
    val normalizedGender = normalizeCitizenGender(selectedGender)
    val isGenderReady = normalizedGender == "LAKI_LAKI" || normalizedGender == "PEREMPUAN"
    val canSubmit = isPhotoReady && isNameReady && isGenderReady && !isSubmitting

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEAF7F1),
                        Color(0xFFF6FBF8),
                        Color(0xFFFFFFFF),
                    ),
                ),
            )
            .testTag("mandatory_profile_screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Top Bar: Badge + Switch Account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ToskaPrimaryDeep.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.25f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = stringResource(R.string.mandatory_profile_badge),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        enabled = !isSubmitting,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, OutlineVariantLight),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("mandatory_profile_logout_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.mandatory_profile_logout_btn),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.mandatory_profile_logout_btn),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Header Card + 3-Step Checklist
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.mandatory_profile_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = ToskaOnPrimaryContainer,
                        )
                        if (userEmail.isNotBlank()) {
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = ToskaPrimaryDeep,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = stringResource(R.string.mandatory_profile_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        // Compact 3-pill live status bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            MandatoryStepPill(
                                label = stringResource(R.string.mandatory_profile_check_photo),
                                isComplete = isPhotoReady,
                                modifier = Modifier.weight(1f),
                            )
                            MandatoryStepPill(
                                label = stringResource(R.string.mandatory_profile_check_name),
                                isComplete = isNameReady,
                                modifier = Modifier.weight(1f),
                            )
                            MandatoryStepPill(
                                label = stringResource(R.string.mandatory_profile_check_gender),
                                isComplete = isGenderReady,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // 1. Mandatory Photo Picker Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(
                        width = if (isPhotoReady) 1.5.dp else 1.dp,
                        color = if (isPhotoReady) ToskaPrimaryDeep.copy(alpha = 0.55f) else OutlineVariantLight,
                    ),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(enabled = !isSubmitting) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(68.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPhotoReady) Color(0xFFE6F4EE) else PendingAmberBg,
                                            CircleShape,
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = if (isPhotoReady) ToskaPrimaryDeep else PendingAmberText.copy(alpha = 0.5f),
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    when {
                                        selectedAvatarUri != null -> {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(selectedAvatarUri)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = stringResource(R.string.mandatory_profile_photo_title),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                            )
                                        }
                                        existingAvatarUrl.isNotBlank() -> {
                                            InitialsAvatar(
                                                name = nameInput.ifBlank { "Warga" },
                                                colorHex = nearbyProfile?.avatarColorHex ?: "#006B5E",
                                                sizeDp = 64,
                                                avatarUrl = existingAvatarUrl,
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Filled.AddAPhoto,
                                                contentDescription = stringResource(R.string.mandatory_profile_pick_btn),
                                                tint = PendingAmberText,
                                                modifier = Modifier.size(26.dp),
                                            )
                                        }
                                    }
                                }

                                if (isPhotoReady) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ToskaPrimaryDeep,
                                        border = BorderStroke(1.5.dp, Color.White),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(22.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.mandatory_profile_photo_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(
                                        if (isPhotoReady) {
                                            R.string.mandatory_profile_photo_ready_hint
                                        } else {
                                            R.string.mandatory_profile_photo_missing_hint
                                        },
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPhotoReady) OpenGreenText else PendingAmberText,
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (!isSubmitting) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    }
                                },
                                enabled = !isSubmitting,
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ToskaPrimaryDeep,
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("mandatory_profile_pick_photo_btn"),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddAPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = stringResource(
                                        if (isPhotoReady) {
                                            R.string.mandatory_profile_change_btn
                                        } else {
                                            R.string.mandatory_profile_pick_btn
                                        },
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        // Compact AI Photo Safety Shield Notice
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF3FBF7),
                            border = BorderStroke(1.dp, OutlineVariantLight.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = ToskaPrimaryDeep,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = stringResource(R.string.ai_photo_safety_form_notice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ToskaOnPrimaryContainer,
                                )
                            }
                        }
                    }
                }

                // 2. Mandatory Name + Gender + District Form Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, OutlineVariantLight),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                enabled = !isSubmitting,
                                label = { Text(stringResource(R.string.mandatory_profile_name_label)) },
                                placeholder = { Text(stringResource(R.string.mandatory_profile_name_placeholder)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = if (isNameReady) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                trailingIcon = if (isNameReady) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = OpenGreenText,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mandatory_profile_name_input"),
                            )
                            Text(
                                text = stringResource(R.string.mandatory_profile_name_helper),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }

                        // Gender Selector (Strictly Laki-laki or Perempuan)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.mandatory_profile_gender_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ToskaOnPrimaryContainer,
                            )
                            Text(
                                text = stringResource(R.string.mandatory_profile_gender_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                MandatoryGenderOptionCard(
                                    label = stringResource(R.string.gender_option_male),
                                    subLabel = "Cak",
                                    icon = Icons.Filled.Male,
                                    selected = normalizedGender == "LAKI_LAKI",
                                    activeColor = Color(0xFF1565C0),
                                    enabled = !isSubmitting,
                                    testTag = "mandatory_gender_male",
                                    onClick = { selectedGender = "LAKI_LAKI" },
                                    modifier = Modifier.weight(1f),
                                )
                                MandatoryGenderOptionCard(
                                    label = stringResource(R.string.gender_option_female),
                                    subLabel = "Ning",
                                    icon = Icons.Filled.Female,
                                    selected = normalizedGender == "PEREMPUAN",
                                    activeColor = Color(0xFFAD1457),
                                    enabled = !isSubmitting,
                                    testTag = "mandatory_gender_female",
                                    onClick = { selectedGender = "PEREMPUAN" },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        // Domicile District Dropdown
                        SurabayaDistrictDropdown(
                            selectedDistrict = selectedDistrict,
                            onSelect = { selectedDistrict = it },
                            enabled = !isSubmitting,
                            label = stringResource(R.string.mandatory_profile_district_label),
                            testTag = "mandatory_district_dropdown",
                        )
                    }
                }

                // Primary Submit Button
                Button(
                    onClick = {
                        onSubmitProfile(
                            nameInput.trim(),
                            normalizedGender,
                            selectedDistrict,
                            selectedAvatarUri,
                        )
                    },
                    enabled = canSubmit,
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color(0xFFDCECE4),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF6B8A7E),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("mandatory_profile_submit_btn"),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (canSubmit) {
                                    Modifier.background(FreshGreenGradient, CircleShape)
                                } else {
                                    Modifier
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSubmitting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = ToskaPrimaryDeep,
                                )
                                Text(
                                    text = stringResource(
                                        if (isScanningPhoto) {
                                            R.string.scanning_photo_ai_label
                                        } else {
                                            R.string.submitting_upload_label
                                        },
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ToskaPrimaryDeep,
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.mandatory_profile_submit_btn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(12.dp),
        )
    }
}

@Composable
private fun MandatoryStepPill(
    label: String,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = if (isComplete) OpenGreenBg else Color(0xFFF3F6F5),
        border = BorderStroke(
            width = 1.dp,
            color = if (isComplete) OpenGreenText.copy(alpha = 0.35f) else OutlineVariantLight,
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = if (isComplete) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isComplete) OpenGreenText else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Medium,
                color = if (isComplete) OpenGreenText else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MandatoryGenderOptionCard(
    label: String,
    subLabel: String,
    icon: ImageVector,
    selected: Boolean,
    activeColor: Color,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) activeColor else Color(0xFFF6FAF8),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) activeColor else OutlineVariantLight,
        ),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (selected) Color.White.copy(alpha = 0.22f) else activeColor.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else activeColor,
                    modifier = Modifier.size(17.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Sapaan $subLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
