import React from 'react';
import OutlinedInput from '@mui/material/OutlinedInput';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import InputLabel from '@mui/material/InputLabel';
import FormControl from '@mui/material/FormControl';
import { useTheme } from '@mui/material/styles';

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
    PaperProps: {
        style: {
            maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
            width: 350,
        },
    },
};


export const CustomMultiSelect = (
    {
        optionList,
        value,
        errors,
        label,
        id,
        multiple,
        onChange,
        ...inputProps }) => {

    const [options, setOptions] = React.useState(value);


    const onHandleChange = (event) => {
        const {
            target: { value },
        } = event;

        setOptions(
            // On autofill we get a stringified value.
            typeof value === 'string' ? value.split(',') : value,
        );
        onChange(event);
    };
    const theme = useTheme();
    function getStyles(name, personName, theme) {
        return {
            fontWeight:
                personName.indexOf(name) === -1
                    ? theme.typography.fontWeightRegular
                    : theme.typography.fontWeightMedium,
        };
    }

    return (
        <FormControl sx={{ m: 1, width: 300 }}>
            <InputLabel id="demo-multiple-name-label">{label}</InputLabel>
            <Select
                id={id}
                multiple
                value={options}
                onChange={onHandleChange}
                input={<OutlinedInput label='Name' />}
                MenuProps={MenuProps}
            >
                {optionList.map((name) => (
                    <MenuItem
                        key={name}
                        value={name}
                        style={getStyles(name, options, theme)}
                    >
                        {name}
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
    )
}