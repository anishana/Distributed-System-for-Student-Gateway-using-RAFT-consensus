import React from 'react';
import MultiSelect from "elevate-ui/MultiSelect";
import { Field } from "formik";


export const ElevateMultiSelect = (
    {
        optionList,
        value,
        errors,
        label,
        id,
        handleChange,
        ...inputProps }) => {



    return (
        <Field
            id={id}
            label={label}
            items={optionList}
            component={MultiSelect}
        />
    )
}