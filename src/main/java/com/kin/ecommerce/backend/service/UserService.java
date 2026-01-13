package com.kin.ecommerce.backend.service;

import com.kin.ecommerce.backend.api.model.RegistrationBody;
import com.kin.ecommerce.backend.api.model.dao.LocalUserDAO;
import com.kin.ecommerce.backend.model.LocalUser;
import com.kin.ecommerce.backend.exception.UserAlreadyExistsException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private LocalUserDAO localUserDAO;

    public UserService(LocalUserDAO localUserDAO, Validator validator) {
        this.localUserDAO = localUserDAO;
    }
    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException {
        if ( localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent() ) {
            throw new UserAlreadyExistsException();
        }
        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        //TODO: add encryption to password
        user.setPassword(registrationBody.getPassword());
        user = localUserDAO.save(user);
        return user;
    }
}
