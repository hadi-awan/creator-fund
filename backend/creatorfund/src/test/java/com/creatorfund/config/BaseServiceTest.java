package com.creatorfund.config;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseServiceTest {
    // Common test utilities and helper methods can go here
    protected static final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
}
