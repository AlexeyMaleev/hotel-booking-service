package com.example.hotels_app.service;

import com.example.hotels_app.entity.User;
import com.example.hotels_app.exception.UserAlreadyExistsException;
import com.example.hotels_app.exception.UserNotFoundException;
import com.example.hotels_app.mapper.UserMapper;
import com.example.hotels_app.model.request.UpsertUserRequest;
import com.example.hotels_app.model.response.UserResponse;
import com.example.hotels_app.repository.UserRepository;
import com.example.hotels_app.statistics.service.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private  final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final KafkaEventProducer eventProducer;

    public UserResponse create(UpsertUserRequest request){

        if (userRepository.existsByName(request.getName())) {
            throw new UserAlreadyExistsException("User with name '" + request.getName() + "' already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email '" + request.getEmail() + "' already exists");
        }

        User user = userMapper.toEntity(request);
        //user.setPassword(request.getPassword());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User userCreated = userRepository.save(user);
        eventProducer.sendRegistrationEvent(userCreated.getId());

        return  userMapper.toResponse(userCreated);
    }

    public UserResponse findByName(String name){
        User user = userRepository.findByName(name)
                .orElseThrow(()-> new UserNotFoundException(name));

        return userMapper.toResponse(user);
    }

    public UserResponse findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));

        return userMapper.toResponse(user);
    }

    public UserResponse update(Long id, UpsertUserRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));
/*
        if(request.getName() != null ){
            user.setName(request.getName());
        }

 */
        if (request.getName() != null && !request.getName().equals(user.getName())) {
            if (userRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new UserAlreadyExistsException("User with name '" + request.getName() + "' already exists");
            }
            user.setName(request.getName());
        }

        if(request.getPassword() != null){
            user.setPassword(request.getPassword());
        }

        /*        if(request.getEmail() != null ){
            user.setEmail(request.getEmail());
        }

         */
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new UserAlreadyExistsException("User with email '" + request.getEmail() + "' already exists");
            }
            user.setEmail(request.getEmail());
        }

        if(request.getRole() != null){
            user.setRole(request.getRole());
        }

        return  userMapper.toResponse(userRepository.save(user));
    }

    public void deleteById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));

        userRepository.delete(user);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
