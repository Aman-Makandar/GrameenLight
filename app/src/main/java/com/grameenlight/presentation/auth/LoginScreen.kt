package com.grameenlight.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.grameenlight.domain.model.UserRole
import com.grameenlight.domain.repository.AuthState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    // Form fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    
    // Mode: true = Sign In, false = Register
    var isSignInMode by remember { mutableStateOf(true) }
    
    // Entrance animations state
    var startAnimations by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        startAnimations = true
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onNavigateToMain()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val screenHeight = maxHeight
        
        // TOP SECTION (60%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.6f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F6E56), Color(0xFF1D9E75))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo with Glow
                AnimatedVisibility(
                    visible = startAnimations,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) + 
                            slideInVertically(initialOffsetY = { -50 }, animationSpec = tween(600, delayMillis = 400))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Glow ring
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFF1D9E75),
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Title
                AnimatedVisibility(
                    visible = startAnimations,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 600))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Grameen-Light",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Citizen-led Streetlight Audit",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Decorative dots
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM SECTION (50%) - Card rises up
        AnimatedVisibility(
            visible = startAnimations,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(600, delayMillis = 300)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.55f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp)
                ) {
                    // Mode Label
                    Text(
                        text = if (isSignInMode) "Welcome back" else "Create your account",
                        color = Color(0xFF1A1A1A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSignInMode) "Sign in to continue" else "Enter your details below",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Error display
                    if (authState is AuthState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = Color(0xFFC62828),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // === REGISTER ONLY: Name field ===
                    AnimatedVisibility(
                        visible = !isSignInMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(14.dp)),
                                placeholder = { Text("Your name", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color(0xFF1D9E75)
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // === BOTH MODES: Email field ===
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(14.dp)),
                        placeholder = { Text("Email address", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF1D9E75)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // === BOTH MODES: Password field ===
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(14.dp)),
                        placeholder = { Text("Password", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF1D9E75)
                        )
                    )
                    
                    // === REGISTER ONLY: Role Selection ===
                    AnimatedVisibility(
                        visible = !isSignInMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "I am a...", color = Color.Gray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RoleCard(
                                    role = UserRole.RESIDENT,
                                    label = "Resident",
                                    icon = Icons.Default.Person,
                                    selectedColor = Color(0xFF1D9E75),
                                    selectedBg = Color(0xFFE8F5F0),
                                    isSelected = selectedRole == UserRole.RESIDENT,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedRole = UserRole.RESIDENT }
                                )
                                RoleCard(
                                    role = UserRole.LINEMAN,
                                    label = "Lineman",
                                    icon = Icons.Default.Build,
                                    selectedColor = Color(0xFF185FA5),
                                    selectedBg = Color(0xFFE6F1FB),
                                    isSelected = selectedRole == UserRole.LINEMAN,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedRole = UserRole.LINEMAN }
                                )
                                RoleCard(
                                    role = UserRole.ADMIN,
                                    label = "Admin",
                                    icon = Icons.Default.Shield,
                                    selectedColor = Color(0xFFBA7517),
                                    selectedBg = Color(0xFFFAEEDA),
                                    isSelected = selectedRole == UserRole.ADMIN,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedRole = UserRole.ADMIN }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Admin and Lineman accounts must be set up by the Panchayat office",
                                color = Color(0xFF888888),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Action Button
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f)
                    
                    val isEnabled = if (isSignInMode) {
                        email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading
                    } else {
                        name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && selectedRole != null && authState !is AuthState.Loading
                    }
                    
                    Button(
                        onClick = {
                            if (isEnabled) {
                                if (isSignInMode) {
                                    viewModel.login(email, password)
                                } else {
                                    viewModel.register(name, email, password, selectedRole!!)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .scale(scale),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color(0xFFCCCCCC)
                        ),
                        contentPadding = PaddingValues(0.dp),
                        interactionSource = interactionSource,
                        enabled = isEnabled
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (isEnabled) Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF1D9E75), Color(0xFF0F6E56))
                                    ) else Brush.linearGradient(listOf(Color(0xFFCCCCCC), Color(0xFFCCCCCC))),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isSignInMode) "Sign In" else "Create Account",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignInMode) "New here? " else "Already have an account? ",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isSignInMode) "Create account" else "Sign in",
                            color = Color(0xFF1D9E75),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                isSignInMode = !isSignInMode
                                // Reset form on switch
                                name = ""
                                email = ""
                                password = ""
                                selectedRole = null
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Your data stays within your village", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    role: UserRole,
    label: String,
    icon: ImageVector,
    selectedColor: Color,
    selectedBg: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    
    Surface(
        modifier = modifier
            .height(84.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) selectedColor else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isSelected) selectedBg else Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) selectedColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) selectedColor else Color.Gray
            )
        }
    }
}
