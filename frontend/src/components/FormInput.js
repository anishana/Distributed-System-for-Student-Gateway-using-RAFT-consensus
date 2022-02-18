import React from 'react';
import { TextField } from '@mui/material';
import { FormGroup } from 'reactstrap';
import FormControl from '@mui/material/FormControl';

import 'bootstrap/dist/css/bootstrap.min.css';

export const FormInput = (
    { placeholder,
        name,
        error,
        label,
        id,
        onChange,
        helperText,
        ...inputProps }) => {


    const onHandleChange = (e) => {
        onChange(e);
    }
    return (
        <FormControl sx={{ m: 3, width: 300 }}>
            <TextField
                label={label}
                id={id}
                name={name}
                placeholder={placeholder}
                variant="standard"
                onChange={onHandleChange}
                helperText={helperText}
                error={error}
            />
        </FormControl>
    )
}